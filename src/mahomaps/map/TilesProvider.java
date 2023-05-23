package mahomaps.map;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

import mahomaps.Gate;
import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.api.YmapsApiBase;
import mahomaps.overlays.TileCacheForbiddenOverlay;
import mahomaps.overlays.TileDownloadForbiddenOverlay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class TilesProvider implements Runnable {

	private static final String INVALID_THREAD_ERR = "Map paint can be performed only from update thread.";
	public final String lang;
	/**
	 * Path to the local folder with tiles. Must be with trailing slash and with
	 * protocol, i.e. file:///E:/ym/
	 */
	public final String localPath;

	/**
	 * Кэш всех загруженых плиток. Данный список должен изменяться ТОЛЬКО из потока
	 * цикла отрисовки. Содержимое объектов списка должно изменяться ТОЛЬКО из
	 * потока скачивания тайлов.
	 * <hr>
	 * Блокировки: <br>
	 * Вектор - перебор объектов для удаления или для выбора для скачивание <br>
	 * Объект вектора - изменения {@link TileCache#state} или удаление из вектора.
	 */
	private Vector cache = new Vector();

	private final Gate downloadGate = new Gate(false);
	private final Gate cacheGate = new Gate(true);

	private boolean paintState = false;

	private Thread networkTh;
	private Thread cacheTh;

	public TilesProvider(String lang, String localPath) {
		if (lang == null)
			throw new NullPointerException("Language must be non-null!");
		this.lang = lang;
		this.localPath = localPath;
	}

	public void Start() {
		if (networkTh != null || cacheTh != null)
			throw new IllegalStateException("Can't start already running tiles provider!");
		networkTh = new Thread(this, "tiles_net");
		cacheTh = new Thread(this, "tiles_cache");
		networkTh.start();
		cacheTh.start();
	}

	public void Stop() {
		if (networkTh != null)
			networkTh.interrupt();
		networkTh = null;
		if (cacheTh != null)
			cacheTh.interrupt();
		cacheTh = null;
	}

	public void CheckCacheFolder() throws IOException, SecurityException {
		FileConnection fc = (FileConnection) Connector.open(localPath);
		try {
			if (!fc.exists())
				fc.mkdir();
		} finally {
			fc.close();
		}
	}

	public void ForceMissingDownload() {
		synchronized (cache) {
			for (int i = 0; i < cache.size(); i++) {
				TileCache tc = (TileCache) cache.elementAt(i);
				synchronized (tc) {
					if (tc.state == TileCache.STATE_MISSING || tc.state == TileCache.STATE_ERROR) {
						tc.state = TileCache.STATE_CACHE_PENDING;
					}
				}
			}
		}

		cacheGate.Reset();
		downloadGate.Reset();
	}

	public void run() {
		if (Thread.currentThread() == networkTh) {
			RunNetwork();
		} else if (Thread.currentThread() == cacheTh) {
			RunCache();
		} else {
			throw new IllegalStateException("Unknown thread type!");
		}
	}

	public void RunCache() {
		try {
			// цикл обработки
			while (true) {
				int i = -1;
				// цикл перебора тайлов в очереди
				while (true) {
					TileCache tc = null;
					synchronized (cache) {
						// инкремент + проверки выхода за границы
						i++;
						int s = cache.size();
						if (i >= s)
							break;
						tc = (TileCache) cache.elementAt(i);
					}
					synchronized (tc) {
						switch (tc.state) {
						case TileCache.STATE_CACHE_PENDING:
							// читаем кэш
							break;
						default:
							// к следующему тайлу
							continue;
						}
					}

					Image img = null;
					if (Settings.cacheMode == Settings.CACHE_FS) {
						img = tryLoadFromFS(tc);
					} else if (Settings.cacheMode == Settings.CACHE_RMS) {
						// TODO
					}

					synchronized (tc) {
						if (img != null) {
							tc.img = img;
							tc.state = TileCache.STATE_READY;
							MahoMapsApp.GetCanvas().requestRepaint();
						} else if (Settings.allowDownload) {
							tc.state = TileCache.STATE_SERVER_PENDING;
							downloadGate.Reset();
						} else {
							tc.state = TileCache.STATE_MISSING;
						}
					}
				} // конец перебора очереди

				cacheGate.Pass();
			}
		} catch (InterruptedException e) {
		}
	}

	public void RunNetwork() {
		try {
			// цикл обработки
			while (true) {
				if (!Settings.allowDownload) {
					try {
						downloadGate.Pass();
					} catch (InterruptedException e) {
						return;
					}
				}

				int idleCount = 0; // счётчик готовых тайлов (если равен длине кэша - ничего грузить не надо)
				int i = -1;
				// цикл перебора тайлов в очереди
				while (true) {
					TileCache tc = null;
					synchronized (cache) {
						// инкремент + проверки выхода за границы
						i++;
						int s = cache.size();
						if (i >= s)
							break;
						tc = (TileCache) cache.elementAt(i);
					}
					synchronized (tc) {
						switch (tc.state) {
						case TileCache.STATE_CACHE_PENDING:
							// ждём чтения кэша
							// к следующему тайлу
							continue;
						case TileCache.STATE_SERVER_PENDING:
							// переключаем состояние
							tc.state = TileCache.STATE_LOADING;
							// начинаем загрузку
							break;
						case TileCache.STATE_LOADING:
							throw new IllegalStateException(
									tc.toString() + " was in loading state before loading sequence!");
						case TileCache.STATE_READY:
							idleCount++;
							// к следующему тайлу
							continue;
						case TileCache.STATE_ERROR:
							// переключаем состояние
							tc.state = TileCache.STATE_LOADING;
							// начинаем загрузку
							break;
						case TileCache.STATE_UNLOADED:
							idleCount++;
							// к следующему тайлу
							continue;
						case TileCache.STATE_MISSING:
							idleCount++;
							// к следующему тайлу
							continue;
						}
					}

					Image img = Settings.allowDownload ? download(tc) : null;

					boolean waitAfterError = false;

					synchronized (tc) {
						if (img == null) {
							if (Settings.allowDownload) {
								tc.state = TileCache.STATE_ERROR;
								waitAfterError = true;
							} else {
								tc.state = TileCache.STATE_MISSING;
							}
						} else {
							tc.img = img;
							tc.state = TileCache.STATE_READY;
							MahoMapsApp.GetCanvas().requestRepaint();
						}
					}

					if (waitAfterError)
						Thread.sleep(4000);
				}

				if (idleCount != cache.size())
					continue;
				downloadGate.Pass();
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Скачивает тайл с сервера и помещает его в кэш. Не обрабатывает статусы тайла!
	 * Не проверяет, разрешена ли загрузка!
	 * 
	 * @param id Тайл для скачки.
	 * @return Тайл, либо null в случае ошибки.
	 * @throws InterruptedException Если поток прерван.
	 */
	private Image download(TileId id) throws InterruptedException {

		HttpConnection hc = null;
		FileConnection fc = null;
		try {
			hc = (HttpConnection) Connector.open(getUrl(id));
			int len = (int) hc.getLength();
			if (len <= 0)
				throw new IOException("Empty responce");
			ByteArrayOutputStream blob = new ByteArrayOutputStream(len);
			byte[] buf = new byte[8192];
			InputStream s = hc.openInputStream();
			while (true) {
				int read = s.read(buf);
				if (read == -1)
					break;
				blob.write(buf, 0, read);
			}
			buf = null;
			s.close();
			hc.close();
			hc = null;
			byte[] blobc = blob.toByteArray();
			blob = null;
			if (Settings.cacheMode == Settings.CACHE_FS) {
				try {
					fc = (FileConnection) Connector.open(getFileName(id), Connector.WRITE);
					fc.create();
					OutputStream os = fc.openOutputStream();
					os.write(blobc);
					os.flush();
					os.close();
				} catch (SecurityException e) {
					MahoMapsApp.Overlays().PushOverlay(new TileCacheForbiddenOverlay());
					Settings.cacheMode = Settings.CACHE_DISABLED;
				} catch (IOException e) {
					// TODO: Выводить на экран алерт что закэшить не удалось
				} finally {
					if (fc != null)
						fc.close();
					fc = null;
				}
			} else if (Settings.cacheMode == Settings.CACHE_RMS) {
				// TODO
			}
			Image img = Image.createImage(blobc, 0, blobc.length);
			return img;
		} catch (SecurityException e) {
			try {
				MahoMapsApp.Overlays().PushOverlay(new TileDownloadForbiddenOverlay());
				Settings.allowDownload = false;
			} catch (RuntimeException e1) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
		} finally {
			if (hc != null) {
				try {
					hc.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	/**
	 * Пытается прочесть изображение тайла из ФС.
	 * 
	 * @param id Тайл для поиска.
	 * @return Изображение, если тайл сохранён, иначе null.
	 */
	private Image tryLoadFromFS(TileId id) {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(getFileName(id), Connector.READ);
			if (!fc.exists()) {
				fc.close();
				return null;
			}
			InputStream s = fc.openInputStream();
			Image img = Image.createImage(s);
			s.close();
			return img;
		} catch (SecurityException e) {
			MahoMapsApp.Overlays().PushOverlay(new TileCacheForbiddenOverlay());
			Settings.cacheMode = Settings.CACHE_DISABLED;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
		} finally {
			if (fc != null) {
				try {
					fc.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	/**
	 * Выполняет операции, необходимые перед очередной отрисовкой.
	 */
	public void BeginMapPaint() {
		if (Thread.currentThread() != MahoMapsApp.thread)
			throw new IllegalThreadStateException(INVALID_THREAD_ERR);
		if (paintState)
			throw new IllegalStateException("Paint is already in progress.");
		paintState = true;

		for (int i = 0; i < cache.size(); i++) {
			((TileCache) cache.elementAt(i)).unuseCount++;
		}
	}

	/**
	 * Выполняет операции, необходимые после очередной отрисовки.
	 */
	public void EndMapPaint() {
		if (Thread.currentThread() != MahoMapsApp.thread)
			throw new IllegalThreadStateException(INVALID_THREAD_ERR);
		if (!paintState)
			throw new IllegalStateException("Paint was not performed.");
		paintState = false;

		synchronized (cache) {
			for (int i = cache.size() - 1; i > -1; i--) {
				TileCache t = (TileCache) cache.elementAt(i);
				if (t.unuseCount > 20) {
					synchronized (t) {
						if (t.state != TileCache.STATE_LOADING) {
							t.state = TileCache.STATE_UNLOADED;
							cache.removeElementAt(i);
						}
					}
				}
			}
		}
	}

	/**
	 * Возвращает объект кэша плитки для отрисовки.
	 *
	 * @param tileId Идентификатор требуемой плитки.
	 * @return Объект кэша плитки в любом из возможных состояний. <b>Может вернуть
	 *         null</b> если координаты плитки не находятся в пределах карты
	 *         (например, Y отрицательный).
	 */
	public TileCache getTile(TileId tileId) {
		int max = 0x1 << tileId.zoom;
		if (tileId.y < 0)
			return null;
		if (tileId.y >= max)
			return null;
		int x = tileId.x;
		while (x < 0)
			x += max;
		while (x >= max)
			x -= max;

		tileId = new TileId(x, tileId.y, tileId.zoom);

		TileCache cached = tryGetExistingFromCache(tileId);
		if (cached != null) {
			cached.unuseCount = 0;
			return cached;
		}

		if (Thread.currentThread() != MahoMapsApp.thread)
			throw new IllegalThreadStateException(INVALID_THREAD_ERR);
		if (!paintState)
			throw new IllegalStateException("Paint isn't performing now.");

		cached = new TileCache(tileId);
		cached.state = TileCache.STATE_CACHE_PENDING;
		synchronized (cache) {
			cache.addElement(cached);
		}

		cacheGate.Reset();

		return cached;
	}

	/**
	 * Возвращает объект кэша плитки из {@link #cache}.
	 * 
	 * @param id Идентификатор требуемой плитки.
	 * @return Объект, если существует, иначе null.
	 */
	private TileCache tryGetExistingFromCache(TileId id) {
		for (int i = 0; i < cache.size(); i++) {
			TileCache tile = (TileCache) cache.elementAt(i);
			if (tile.is(id))
				return tile;
		}

		return null;
	}

	private String getUrl(TileId tileId) {
		String url = "https://core-renderer-tiles.maps.yandex.net/tiles?l=map&lang=" + lang + "&x=" + tileId.x + "&y="
				+ tileId.y + "&z=" + tileId.zoom;
		if (Settings.proxyTiles) {
			return "http://nnp.nnchan.ru/mahoproxy.php?u=" + YmapsApiBase.EncodeUrl(url);
		}
		return url;
	}

	private String getFileName(TileId id) {
		return localPath + "tile_" + id.x + "_" + id.y + "_" + id.zoom;
	}

}

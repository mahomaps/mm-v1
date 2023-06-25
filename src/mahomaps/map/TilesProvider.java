package mahomaps.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import mahomaps.Gate;
import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.api.YmapsApiBase;
import mahomaps.overlays.TileCacheForbiddenOverlay;
import mahomaps.overlays.TileDownloadForbiddenOverlay;
import mahomaps.overlays.CacheFailedOverlay;

public class TilesProvider implements Runnable {

	private static final String INVALID_THREAD_ERR = "Map paint can be performed only from update thread.";
	public final String lang;
	/**
	 * Path to the local folder with tiles. Must be with trailing slash and with
	 * protocol, i.e. file:///E:/ym/
	 */
	private String localPath;

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
	private final Object cacheAccessLock = new Object();

	private boolean paintState = false;

	private Thread networkTh;
	private Thread cacheTh;

	public static final String[] tilesUrls = new String[] {
			// scheme
			"https://core-renderer-tiles.maps.yandex.net/tiles?l=map&lang=",
			// sat
			"https://core-sat.maps.yandex.net/tiles?l=sat&lang=",
			// hybrid
			// next PR
	};

	public TilesProvider(String lang) {
		if (lang == null)
			throw new NullPointerException("Language must be non-null!");
		this.lang = lang;
	}

	public void Start() {
		if (networkTh != null || cacheTh != null)
			throw new IllegalStateException("Can't start already running tiles provider!");
		networkTh = new Thread(this, "Tiles downloader");
		cacheTh = new Thread(this, "Tiles cache reader");
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

	public void InitFSCache(String path) throws IOException, SecurityException {
		FileConnection fc = (FileConnection) Connector.open(path);
		try {
			if (!fc.exists())
				fc.mkdir();
			localPath = path;
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
					{
						// attempt to check state without taking lock
						int s = tc.state;
						switch (s) {
						case TileCache.STATE_CACHE_PENDING:
						case TileCache.STATE_CACHE_LOADING:
							// we need to take lock and recheck.
							break;
						default:
							// we can't transit from any futher state to cache reads.
							// next tile!
							continue;
						}
					}
					synchronized (tc) {
						switch (tc.state) {
						case TileCache.STATE_CACHE_PENDING:
							// состояние
							tc.state = TileCache.STATE_CACHE_LOADING;
							// читаем кэш
							break;
						case TileCache.STATE_CACHE_LOADING:
							throw new IllegalStateException(
									tc.toString() + " was in cache loading state before loading sequence!");
						default:
							throw new IllegalStateException(tc.toString()
									+ " changed its state from cache pending/wait to something else during monitor catch!");
						}
					}

					Image img = null;
					if (Settings.cacheMode == Settings.CACHE_FS && localPath != null) {
						img = tryLoadFromFS(tc);
					} else if (Settings.cacheMode == Settings.CACHE_RMS) {
						img = tryLoadFromRMS(tc);
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
				int l = -1;
				boolean queueChanged = false;
				// цикл перебора тайлов в очереди
				while (true) {
					TileCache tc = null;
					synchronized (cache) {
						// инкремент + проверки выхода за границы
						i++;
						int s = cache.size();
						if (l == -1)
							l = s;
						else if (l != s)
							queueChanged = true;
						if (i >= s)
							break;
						tc = (TileCache) cache.elementAt(i);
					}
					// if tile is ready already, it can't be downloaded again. Skipping without
					// taking lock.
					if (tc.state == TileCache.STATE_READY) {
						idleCount++;
						continue;
					}
					synchronized (tc) {
						switch (tc.state) {
						case TileCache.STATE_CACHE_PENDING:
						case TileCache.STATE_CACHE_LOADING:
							// ждём чтения кэша
							// к следующему тайлу
							continue;
						case TileCache.STATE_SERVER_PENDING:
							// переключаем состояние
							tc.state = TileCache.STATE_SERVER_LOADING;
							// начинаем загрузку
							break;
						case TileCache.STATE_SERVER_LOADING:
							throw new IllegalStateException(
									tc.toString() + " was in server loading state before loading sequence!");
						case TileCache.STATE_READY:
							idleCount++;
							// к следующему тайлу
							continue;
						case TileCache.STATE_ERROR:
							// переключаем состояние
							tc.state = TileCache.STATE_SERVER_LOADING;
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

				if (idleCount != cache.size() || queueChanged)
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
			hc = (HttpConnection) Connector.open(getUrl(id) + MahoMapsApp.getConnectionParams());
			int len = (int) hc.getLength();
			ByteArrayOutputStream blob = len <= 0 ? new ByteArrayOutputStream() : new ByteArrayOutputStream(len);
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
			if (Settings.cacheMode == Settings.CACHE_FS && localPath != null) {
				synchronized (cacheAccessLock) {
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
						MahoMapsApp.Overlays().PushOverlay(new CacheFailedOverlay());
						Settings.cacheMode = Settings.CACHE_DISABLED;
					} finally {
						if (fc != null)
							fc.close();
						fc = null;
					}
				}
			} else if (Settings.cacheMode == Settings.CACHE_RMS) {
				synchronized (cacheAccessLock) {
					try {
						RecordStore r = RecordStore.openRecordStore(getRmsName(id), true);
						if (r.getNumRecords() == 0)
							r.addRecord(new byte[1], 0, 1);
						r.setRecord(1, blobc, 0, blobc.length);
						r.closeRecordStore();
					} catch (RecordStoreFullException e) {
						// TODO: Выводить алерт что место закончилось
					} catch (Exception e) {
						// TODO: Выводить на экран алерт что закэшить не удалось
						e.printStackTrace();
					}
				}
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
		synchronized (cacheAccessLock) {
			FileConnection fc = null;
			try {
				fc = (FileConnection) Connector.open(getFileName(id), Connector.READ);
				if (!fc.exists()) {
					fc.close();
					return null;
				}
				InputStream s = fc.openInputStream();
				ByteArrayOutputStream o = new ByteArrayOutputStream();
				byte[] buf = new byte[512];
				int read;
				while ((read = s.read(buf)) != -1) {
					o.write(buf, 0, read);
				}
				s.close();
				byte[] b = o.toByteArray();
				o.close();
				return Image.createImage(b, 0, b.length);
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
	}

	/**
	 * Пытается прочесть изображение тайла из RMS.
	 *
	 * @param id Тайл для поиска.
	 * @return Изображение, если тайл сохранён, иначе null.
	 */
	private Image tryLoadFromRMS(TileId id) {
		synchronized (cacheAccessLock) {
			byte[] b = null;
			try {
				RecordStore r = RecordStore.openRecordStore(getRmsName(id), true);
				if (r.getNumRecords() > 0) {
					b = r.getRecord(1);
				}
				r.closeRecordStore();
			} catch (RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}

			if (b != null) {
				return Image.createImage(b, 0, b.length);
			}

			return null;
		}
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

		// lock is not needed because it's modified only from tile get / tile cleanup,
		// which happens in this thread.
		for (int i = 0; i < cache.size(); i++) {
			((TileCache) cache.elementAt(i)).unuseCount++;
		}
	}

	/**
	 * Выполняет операции, необходимые после очередной отрисовки.
	 */
	public void EndMapPaint(MapState ms) {
		if (Thread.currentThread() != MahoMapsApp.thread)
			throw new IllegalThreadStateException(INVALID_THREAD_ERR);
		if (!paintState)
			throw new IllegalStateException("Paint was not performed.");
		paintState = false;

		final int reqZoom = ms.zoom;

		boolean removed = false;
		synchronized (cache) {
			for (int i = cache.size() - 1; i > -1; i--) {
				TileCache t = (TileCache) cache.elementAt(i);
				if (t.unuseCount > 20 || t.zoom != reqZoom) {
					synchronized (t) {
						switch (t.state) {
						case TileCache.STATE_CACHE_LOADING:
						case TileCache.STATE_SERVER_LOADING:
							// we can't remove this tile
							continue;
						default:
							t.state = TileCache.STATE_UNLOADED;
							cache.removeElementAt(i);
							removed = true;
							break;
						}
					}
				}
			}
		}

		if (removed)
			cacheGate.Reset();
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
		if (!paintState)
			throw new IllegalStateException("Paint was not performing now, can't get tile!");
		if (Thread.currentThread() != MahoMapsApp.thread)
			throw new IllegalThreadStateException(INVALID_THREAD_ERR);

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

		tileId = new TileId(x, tileId.y, tileId.zoom, tileId.map);

		TileCache cached = null;

		// modified only from cleanup - lock is not needed
		for (int i = 0; i < cache.size(); i++) {
			TileCache tile = (TileCache) cache.elementAt(i);
			if (tile.is(tileId)) {
				cached = tile;
				break;
			}
		}

		if (cached != null) {
			cached.unuseCount = 0;
			return cached;
		}

		cached = new TileCache(tileId);
		cached.state = TileCache.STATE_CACHE_PENDING;
		// avoid modifying vector during bounds checks
		synchronized (cache) {
			cache.addElement(cached);
		}

		cacheGate.Reset();

		return cached;
	}

	private String getUrl(TileId tileId) {
		String url = tilesUrls[tileId.map] + lang + "&x=" + tileId.x + "&y=" + tileId.y
				+ "&z=" + tileId.zoom;
		if (Settings.proxyTiles) {
			return Settings.proxyServer + YmapsApiBase.EncodeUrl(url);
		}
		return url;
	}

	private String getFileName(TileId id) {
		return localPath + getRmsName(id);
	}

	private String getRmsName(TileId id) {
		return "tile_" + lang + "_" + id.x + "_" + id.y + "_" + id.zoom + (id.map == 1 ? "_s" : "");
	}

	public int GetCachedTilesCount() {
		synchronized (cacheAccessLock) {
			if (Settings.cacheMode == Settings.CACHE_RMS) {
				String[] names = RecordStore.listRecordStores();
				if (names == null)
					return 0;
				int c = 0;
				for (int i = 0; i < names.length; i++) {
					if (names[i].indexOf("tile_") == 0)
						c++;
				}
				return c;
			}
			if (Settings.cacheMode == Settings.CACHE_FS) {
				FileConnection fc = null;
				try {
					fc = (FileConnection) Connector.open(localPath, Connector.READ);
					Enumeration e = fc.list();
					int c = 0;
					while (e.hasMoreElements()) {
						String object = (String) e.nextElement();
						if (object.indexOf("tile_") == 0)
							c++;
					}
					return c;
				} catch (Exception e) {
				} finally {
					if (fc != null)
						try {
							fc.close();
						} catch (IOException e) {
						}
				}
			}
		}
		return 0;
	}

	/**
	 * Удаляет ВСЕ тайлы из выбранного хранилища кэша (рмс/фс).
	 */
	public void ClearCache() {
		synchronized (cacheAccessLock) {
			if (Settings.cacheMode == Settings.CACHE_RMS) {
				String[] names = RecordStore.listRecordStores();
				if (names == null)
					return;
				for (int i = 0; i < names.length; i++) {
					if (names[i].indexOf("tile_") == 0) {
						try {
							RecordStore.deleteRecordStore(names[i]);
						} catch (RecordStoreNotFoundException e) {
						} catch (RecordStoreException e) {
						}
					}
				}
			}
			if (Settings.cacheMode == Settings.CACHE_FS) {
				FileConnection fc = null;
				try {
					fc = (FileConnection) Connector.open(localPath, Connector.READ);
					Enumeration e = fc.list();
					while (e.hasMoreElements()) {
						String name = (String) e.nextElement();
						if (name.indexOf("tile_") == 0) {
							FileConnection fc2 = null;
							try {
								fc2 = (FileConnection) Connector.open(localPath + name, Connector.WRITE);
								fc2.delete();
							} catch (Exception e2) {
							} finally {
								if (fc2 != null)
									try {
										fc2.close();
									} catch (IOException e3) {
									}
							}
						}
					}
				} catch (Exception e) {
				} finally {
					if (fc != null)
						try {
							fc.close();
						} catch (IOException e) {
						}
				}
			}
		}
	}
}

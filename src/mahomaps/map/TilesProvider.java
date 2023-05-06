package mahomaps.map;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class TilesProvider extends Thread {

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

	private final Image transPixel;

	private final Object downloadLock = new Object();

	private boolean paintState = false;

	public TilesProvider(String lang, String localPath) throws IOException {
		if (lang == null)
			throw new NullPointerException("Language must be non-null!");
		this.lang = lang;
		this.localPath = localPath;
		transPixel = Image.createRGBImage(new int[] { 0 }, 1, 1, true);
		FileConnection fc = (FileConnection) Connector.open(localPath);
		if (!fc.exists())
			fc.mkdir();
		fc.close();
	}

	public void run() {
		try {
			while (true) {
				int idleCount = 0;
				int errCount = 0;
				int i = -1;
				while (true) {
					TileCache tc = null;
					synchronized (cache) {
						i++;
						int s = cache.size();
						if (i >= s)
							break;
						tc = (TileCache) cache.elementAt(i);
					}
					synchronized (tc) {
						if (tc.state == TileCache.STATE_READY || tc.state == TileCache.STATE_UNLOADED) {
							idleCount++;
							continue;
						}
						tc.state = TileCache.STATE_LOADING;
					}

					Image img = download(tc);
					synchronized (tc) {
						if (img == null) {
							tc.state = TileCache.STATE_ERROR;
							errCount++;
						} else {
							tc.img = img;
							tc.state = TileCache.STATE_READY;
						}
					}

				}

				if (errCount != 0)
					continue;
				if (idleCount != cache.size())
					continue;
				synchronized (downloadLock) {
					downloadLock.wait(4000);
				}

			}
		} catch (InterruptedException e) {
		}
	}

	private Image download(TileId id) {
		if (!Settings.allowDownload)
			return null;

		HttpConnection hc = null;
		FileConnection fc = null;
		try {
			hc = (HttpConnection) Connector.open(getUrl(id));
			ByteArrayOutputStream blob = new ByteArrayOutputStream((int) hc.getLength());
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
			fc = (FileConnection) Connector.open(getFileName(id), Connector.WRITE);
			fc.create();
			OutputStream os = fc.openOutputStream();
			os.write(blobc);
			os.flush();
			os.close();
			fc.close();
			Image img = Image.createImage(blobc, 0, blobc.length);
			return img;
		} catch (IOException e) {
			e.printStackTrace();
			if (hc != null) {
				try {
					hc.close();
				} catch (IOException ex) {
				}
			}
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
			fc.close();
			fc = null;
			return img;
		} catch (IOException e) {
			e.printStackTrace();
			if (fc != null) {
				try {
					fc.close();
				} catch (IOException ex) {
				}
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
				if (t.unuseCount > 100) {
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
		Image img = tryLoadFromFS(tileId);
		if (img != null) {
			cached.img = img;
			cached.state = TileCache.STATE_READY;
		}
		cache.addElement(cached);

		synchronized (downloadLock) {
			downloadLock.notifyAll();
		}

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
		return "https://core-renderer-tiles.maps.yandex.net/tiles?l=map&lang=" + lang + "&x=" + tileId.x + "&y="
				+ tileId.y + "&z=" + tileId.zoom;
	}

	private String getFileName(TileId id) {
		return localPath + "tile_" + id.x + "_" + id.y + "_" + id.zoom;
	}

}

package mahomaps.map;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TilesProvider extends Thread {

	public final String lang;
	/**
	 * Path to the local folder with tiles. Must be with trailing slash and with
	 * protocol, i.e. file:///E:/ym/
	 */
	public final String localPath;

	private Vector cache = new Vector();

	private final Image transPixel;

	private final Object downloadLock = new Object();

	private TileId next;

	public TilesProvider(String lang, String localPath) {
		if (lang == null)
			throw new NullPointerException("Language must be non-null!");
		this.lang = lang;
		this.localPath = localPath;
		transPixel = Image.createRGBImage(new int[] { 0 }, 1, 1, true);
	}

	public void run() {
		try {
			while (true) {
				if (next == null)
					downloadLock.wait(1000);

				TileId id = next;
				next = null;
				if (id != null)
					download(id);

			}
		} catch (InterruptedException e) {
		}
	}

	private void download(TileId id) {
		HttpConnection hc = null;
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
			Image img = Image.createImage(blobc, 0, blobc.length);
			TileCache tile = new TileCache(id, img);
			cache.addElement(tile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (hc != null)
				try {
					hc.close();
				} catch (IOException e) {
				}
		}
	}

	public Image getTile(TileId tileId) {
		int max = 0x1 << tileId.zoom;
		if (tileId.y < 0)
			return transPixel;
		if (tileId.y >= max)
			return transPixel;
		int x = tileId.x;
		while (x < 0)
			x += max;
		while (x >= max)
			x -= max;

		tileId = new TileId(x, tileId.y, tileId.zoom);

		Image cached = tryGetFromCache(tileId);
		if (cached != null)
			return cached;

		next = tileId;
		downloadLock.notifyAll();

		return transPixel;
	}

	private Image tryGetFromCache(TileId id) {
		for (int i = 0; i < cache.size(); i++) {
			TileCache tile = (TileCache) cache.elementAt(i);
			if (tile.is(id))
				return tile.img;
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

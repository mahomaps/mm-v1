package mahomaps.map;

import javax.microedition.lcdui.Image;
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

}

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

	public TilesProvider(String lang, String localPath) {
		if (lang == null)
			throw new NullPointerException("Language must be non-null!");
		this.lang = lang;
		this.localPath = localPath;
	}

	public void run() {
	}

	public Image getTile(TileId tileId) {
		return null;
	}

	private String getUrl(TileId tileId) {
		return "https://core-renderer-tiles.maps.yandex.net/tiles?l=map&lang=" + lang + "&x=" + tileId.x + "&y="
				+ tileId.y + "&z=" + tileId.zoom;
	}

}

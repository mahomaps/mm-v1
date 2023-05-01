package mahomaps.map;

import javax.microedition.lcdui.Image;

public class TileCache extends TileId {

	public final Image img;

	public TileCache(int x, int y, int zoom, Image img) {
		super(x, y, zoom);
		this.img = img;
	}

	public TileCache(TileId id, Image img) {
		super(id.x, id.y, id.zoom);
		this.img = img;
	}

	public boolean is(TileId id) {
		return id.x == x && id.y == y && id.zoom == zoom;
	}
}

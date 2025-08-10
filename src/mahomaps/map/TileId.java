package mahomaps.map;

public class TileId {

	public final int x;
	public final int y;
	public final int zoom;
	public final int map;

	public TileId(int x, int y, int zoom, int map) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.map = map;
	}

	public final boolean is(TileId id) {
		return id.x == x && id.y == y && id.zoom == zoom && id.map == map;
	}
}

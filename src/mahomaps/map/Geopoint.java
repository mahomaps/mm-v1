package mahomaps.map;

import mahomaps.MahoMapsApp;
import mahomaps.screens.MapCanvas;

public class Geopoint {
	// с севера на юг (вертикаль)
	public double lat;
	// по экватору (горизонталь)
	public double lon;

	public int type;

	public Geopoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public int GetScreenX(MapCanvas map) {
		int tilesCount = 1 << map.zoom;
		double tile = (tilesCount * (lon + 180d)) / 360d;
		tile -= map.tileX;
		tile *= 256;
		tile += map.xOffset;
		return (int) tile;
	}

	public int GetScreenY(MapCanvas map) {
		double latR = lat * Math.PI / 180d;
		double tg = Math.tan(latR / 2 + Math.PI / 4);
		double linear = MahoMapsApp.ln(tg);
		double linearLocal = linear * coef; // [-128; 128]
		double linearAbs = 128 - linearLocal; // [0; 256]

		int tilesCount = 1 << map.zoom;

		double py = (tilesCount * linearAbs) - (map.tileY * 256) + map.yOffset;

		return (int) py;
	}

	public static final int LOCATION = 0;
	public static final int ROUTE_A = 1;
	public static final int ROUTE_B = 2;
	public static final int POI_SEARCH = 3;
	public static final int POI_ATTENTION = 4;

	public static final double PI = 3.14159265358979323846;
	public static final double coef = 40.584111828639d;
}

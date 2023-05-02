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
		double latR = Math.toRadians(lat);
		double tg = Math.tan(latR / 2 + Math.PI / 4);
		double linear = MahoMapsApp.ln(tg);

		double absLinear = (flipPoint * 2) - linear;

		int tilesCount = 1 << map.zoom;
		double tile = (tilesCount * absLinear * coef) / 256d;
		tile -= map.tileY;
		tile *= 256;
		tile += map.yOffset;
		return (int) tile;
	}

	public static final int LOCATION = 0;
	public static final int ROUTE_A = 1;
	public static final int ROUTE_B = 2;
	public static final int POI_SEARCH = 3;
	public static final int POI_ATTENTION = 4;

	public static final double PI = 3.14159265358979323846;
	public static final double LOG2 = 0.6931471805599453;
	public static final double flipPoint = 1.48404722d;
	public static final double coef = 40.8d;
}

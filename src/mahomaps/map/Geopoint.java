package mahomaps.map;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.screens.MapCanvas;

public class Geopoint {
	// с севера на юг (вертикаль)
	public double lat;
	// по экватору (горизонталь)
	public double lon;

	public int type;
	public int color;

	public Object object;

	public static Image locationIcons;
	public static Image search;

	static {
		try {
			locationIcons = Image.createImage("/geo40.png");
			search = Image.createImage("/search40.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Geopoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public int GetScreenX(MapState ms) {
		int tilesCount = 1 << ms.zoom;
		double tile = (tilesCount * (lon + 180d)) / 360d;
		tile -= ms.tileX;
		tile *= 256;
		tile += ms.xOffset;
		return (int) tile;
	}

	public int GetScreenY(MapState ms) {
		return GetY(ms.zoom, ms.tileY, ms.yOffset);
	}

	public int GetY(int zoom, int tileY, int yOffset) {
		double latR = lat * Math.PI / 180d;
		double tg = Math.tan(latR / 2 + Math.PI / 4);
		double linear = MahoMapsApp.ln(tg * CalcLatCorr(latR));
		double linearLocal = linear * LAT_COEF; // [-128; 128]
		double linearAbs = 128 - linearLocal; // [0; 256]

		int tilesCount = 1 << zoom;

		double py = (tilesCount * linearAbs) - (tileY * 256) + yOffset;

		return (int) py;
	}

	private static double CalcLatCorr(double latR) {
		double base = (1 - EL_CORR * Math.sin(latR)) / (1 + EL_CORR * Math.sin(latR));
		return MahoMapsApp.pow(base, (EL_CORR / 2));
	}

	public void paint(Graphics g, MapState ms) {
		int px = GetScreenX(ms);
		int py = GetScreenY(ms);
		int s;
		switch (type) {
		case POI_SELECT:
		case POI_MARK:
			s = search.getWidth() / 8;
			g.drawRegion(search, s * (color + 4), 0, s, search.getHeight(), 0, px, py,
					Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case POI_SEARCH:
			s = search.getWidth() / 8;
			g.drawRegion(search, s * color, 0, s, search.getHeight(), 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case LOCATION:
			s = locationIcons.getWidth() / 4;
			g.drawRegion(locationIcons, s * color, s * Settings.geoLook, s, s, 0, px, py,
					Graphics.VCENTER | Graphics.HCENTER);
			break;
		case ROUTE_A:
		case ROUTE_B:
		case ROUTE_C:
			s = search.getWidth() / 8;
			g.drawRegion(search, s * (color + 4), 0, s, search.getHeight(), 0, px, py,
					Graphics.BOTTOM | Graphics.HCENTER);
			break;
		}
	}

	public boolean isTouched(MapCanvas map, MapState ms, int x, int y) {
		x -= map.getWidth() / 2;
		y -= map.getHeight() / 2;
		if (Math.abs(GetScreenX(ms) - x) < 14) {
			int py = GetScreenY(ms);
			if (y > py)
				return false;
			if (y > py - 40)
				return true;
		}
		return false;
	}

	public String toString() {
		boolean latS = lat >= 0;
		double latF = Math.abs(lat) % 1;
		int latD = (int) (Math.abs(lat) - latF);

		boolean lonS = lon >= 0;
		double lonF = Math.abs(lon) % 1;
		int lonD = (int) (Math.abs(lon) - lonF);

		StringBuffer sb = new StringBuffer();

		if (latS)
			sb.append('-');
		sb.append(latD);
		sb.append(String.valueOf(latF).substring(1));

		sb.append(' ');

		if (lonS)
			sb.append('-');
		sb.append(lonD);
		sb.append(String.valueOf(lonF).substring(1));

		return sb.toString();
	}

	public static final int COLOR_RED = 0;
	public static final int COLOR_GREEN = 1;
	public static final int COLOR_GRAY = 2;
	public static final int COLOR_BLUE = 3;

	/**
	 * Shevron mark. Usually means manually selected objects.
	 */
	public static final int POI_SELECT = 0;
	/**
	 * Exclamation mark. Usually means additional POIs around object.
	 */
	public static final int POI_MARK = 1;
	/**
	 * Search mark. Search results.
	 */
	public static final int POI_SEARCH = 2;
	public static final int LOCATION = 3;
	public static final int ROUTE_A = 4;
	public static final int ROUTE_B = 5;
	public static final int ROUTE_C = 6;

	public static final double PI = 3.14159265358979323846;
	public static final double LAT_COEF = 40.74366567247929d;
	public static final double EL_CORR = 0.0818191909289069d;
}

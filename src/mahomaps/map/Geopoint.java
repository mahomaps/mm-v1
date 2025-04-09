/*
Copyright (c) 2023 Fyodor Ryzhov
Copyright (c) 2023 Arman Jussupgaliyev
*/
package mahomaps.map;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.screens.MapCanvas;

public class Geopoint {
	/**
	 * Градусы от экватора на север (вертикаль)
	 */
	public double lat;
	/**
	 * Градусы по экватору (горизонталь)
	 */
	public double lon;

	public int type;
	public int color;

	public Object object;

	public static Image locationIcons;
	public static Image commonPs;
	public static Image route;

	static {
		try {
			locationIcons = Image.createImage("/geo40.png");
			commonPs = Image.createImage("/points40.png");
			route = Image.createImage("/route40.png");
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

	public boolean IsValid() {
		return Math.abs(lat) <= 85 && Math.abs(lon) < 180;
	}

	public void paint(Graphics g, MapState ms) {
		int px = GetScreenX(ms);
		int py = GetScreenY(ms);
		int s;
		switch (type) {
		case POI_SELECT:
			s = commonPs.getWidth() / 4;
			g.drawRegion(commonPs, s * color, 80, s, 40, 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case POI_MARK:
			s = commonPs.getWidth() / 4;
			g.drawRegion(commonPs, s * color, 40, s, 40, 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case POI_SEARCH:
			s = commonPs.getWidth() / 4;
			g.drawRegion(commonPs, s * color, 0, s, 40, 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case LOCATION:
			s = locationIcons.getWidth() / 2;
			g.drawRegion(locationIcons, color == 0 ? 0 : s, s * Settings.geoLook, s, s, 0, px, py,
					Graphics.VCENTER | Graphics.HCENTER);
			break;
		case ROUTE_A:
			g.drawRegion(route, 0, 0, route.getWidth() / 3, route.getHeight(), 0, px, py,
					Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case ROUTE_B:
			s = route.getWidth() / 3;
			g.drawRegion(route, s, 0, s, route.getHeight(), 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case ROUTE_C:
			s = route.getWidth() / 3;
			g.drawRegion(route, s * 2, 0, s, route.getHeight(), 0, px, py, Graphics.BOTTOM | Graphics.HCENTER);
			break;
		case ROUTE_VERTEX:
			g.setColor(0xff0000);
			g.fillRect(px - 2, py - 2, 4, 4);
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

	public String[] GetRounded() {
		boolean latS = lat >= 0;
		double latF = Math.abs(lat) % 1;
		int latD = (int) (Math.abs(lat) - latF);
		String latFS = String.valueOf(latF).substring(1);
		if (latFS.length() > 7)
			latFS = latFS.substring(0, 7);

		boolean lonS = lon >= 0;
		double lonF = Math.abs(lon) % 1;
		int lonD = (int) (Math.abs(lon) - lonF);
		String lonFS = String.valueOf(lonF).substring(1);
		if (lonFS.length() > 7)
			lonFS = lonFS.substring(0, 7);

		StringBuffer sb = new StringBuffer();
		String[] r = new String[2];

		if (!latS)
			sb.append('-');
		sb.append(latD);
		sb.append(latFS);

		r[0] = sb.toString();
		sb.setLength(0);

		if (!lonS)
			sb.append('-');
		sb.append(lonD);
		sb.append(lonFS);

		r[1] = sb.toString();
		return r;
	}

	public String toString() {
		String[] r = GetRounded();
		return r[0] + " " + r[1];
	}

	/**
	 * Получает точку на экране по координатам экрана.
	 *
	 * @param ms Состояние карты.
	 * @param x  X относительно центра (центр = 0)
	 * @param y  Y относительно центра (центр = 0)
	 * @return Точка.
	 */
	public static Geopoint GetAtCoords(MapState ms, int x, int y) {
		ms = ms.Clone();
		int tilesCount = 1 << ms.zoom;
		double dx = x;
		dx -= ms.xOffset;
		dx /= 256;
		dx += ms.tileX;
		dx *= 360d;
		dx /= tilesCount;
		double lon = dx - 180d;

		Geopoint g = new Geopoint(0, lon);
		double step = 60d;
		while (true) {
			double or = g.lat;
			if (Math.abs(or) > 91d) {
				g.lat = or > 0 ? 90 : -90;
				return g;
			}
			int zero = Math.abs(g.GetScreenY(ms) - y);
			if (zero <= 2)
				break;
			g.lat = or + step;
			int plus = Math.abs(g.GetScreenY(ms) - y);
			g.lat = or - step;
			int minus = Math.abs(g.GetScreenY(ms) - y);

			if (zero < Math.min(minus, plus)) {
				g.lat = or;
			} else if (minus < plus) {
				g.lat = or - step;
			} else {
				g.lat = or + step;
			}
			step /= 2d;
			if (step < 0.000001d) {
				System.out.println("Step too small, bumping");
				step = 0.05d;
			}
		}

		return g;
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
	public static final int ROUTE_VERTEX = 7;

	public static final double PI = 3.14159265358979323846;
	public static final double LAT_COEF = 40.74366567247929d;
	public static final double EL_CORR = 0.0818191909289069d;
}

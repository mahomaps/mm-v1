package mahomaps.map;

import javax.microedition.lcdui.Graphics;

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

	public void paint(Graphics g, MapCanvas map) {
		int px = GetScreenX(map);
		int py = GetScreenY(map);
		if (type == POI_SELECT || type == POI_SEARCH) {
			g.setColor(255, 0, 0);
			g.drawLine(px - 5, py, px - 1, py);
			g.drawLine(px, py - 5, px, py - 1);
			g.drawLine(px + 1, py, px + 5, py);
			g.drawLine(px, py + 1, px, py + 5);
			g.drawRect(px - 6, py - 6, 12, 12);
		} else if (type == LOCATION) {
			g.setColor(255, 0, 0);
			g.fillArc(px - 10, py - 10, 20, 20, 0, 360);
			g.setColor(-1);
			g.drawChar('Я', px, py - g.getFont().getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
		} else if (type == ROUTE_A || type == ROUTE_B) {
			g.setColor(0, 0, 255);
			g.fillArc(px - 10, py - 10, 20, 20, 0, 360);
			g.setColor(-1);
			g.drawChar('x', px, py - g.getFont().getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
		}
	}

	public static final int POI_SELECT = 0;
	public static final int POI_SEARCH = 1;
	public static final int LOCATION = 2;
	public static final int ROUTE_A = 3;
	public static final int ROUTE_B = 4;

	public static final double PI = 3.14159265358979323846;
	public static final double coef = 40.584111828639d;
}

package mahomaps.map;

import javax.microedition.lcdui.Graphics;

import mahomaps.screens.MapCanvas;

public class Line {
	public final Geopoint start;
	private final Geopoint[] source;

	private int forZoom = -1;
	private int[] cache = null;

	public Line(Geopoint start, Geopoint[] source) {
		this.start = start;
		this.source = source;
	}

	private void Invalidate(MapState ms) {
		ms = ms.Clone();
		forZoom = ms.zoom;
		int xa = start.GetScreenX(ms);
		int ya = start.GetScreenY(ms);
		cache = new int[source.length * 2];
		for (int i = 0; i < source.length; i++) {
			int px = source[i].GetScreenX(ms);
			int py = source[i].GetScreenY(ms);
			cache[i * 2] = px - xa;
			cache[i * 2 + 1] = py - ya;
		}
	}

	public synchronized void Draw(Graphics g, MapCanvas map) {
		if (map.state.zoom != forZoom)
			Invalidate(map.state);

		int px = start.GetScreenX(map.state);
		int py = start.GetScreenY(map.state);

		g.setColor(0xff0000);
		for (int i = 2; i < cache.length; i += 2) {
			int x1 = px + cache[i - 2];
			int y1 = py + cache[i - 1];
			int x2 = px + cache[i];
			int y2 = py + cache[i + 1];
			g.drawLine(x1, y1, x2, y2);
		}
	}

}
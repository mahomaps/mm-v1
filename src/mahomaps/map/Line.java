/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.map;

import javax.microedition.lcdui.Graphics;

import mahomaps.screens.MapCanvas;

public class Line {
	public final Geopoint start;
	private final Geopoint[] source;

	private int forZoom = -1;
	private int[] cache = null;
	private int thickness = 2;
	public int drawFrom = 0;

	public Line(Geopoint start, Geopoint[] source) {
		this.start = start;
		this.source = source;
	}

	private void Invalidate(MapState ms) {
		ms = ms.Clone();
		forZoom = ms.zoom;
		int xa = start.GetScreenX(ms);
		int ya = start.GetScreenY(ms);
		int[] temp = new int[source.length * 2];
		int ti = -1;
		for (int i = Math.max(0, drawFrom); i < source.length; i++) {
			int px = source[i].GetScreenX(ms) - xa;
			int py = source[i].GetScreenY(ms) - ya;
			if (ti >= 0) {
				if (temp[ti * 2] != px || temp[ti * 2 + 1] != py)
					ti++;
			} else {
				ti++;
			}
			temp[ti * 2] = px;
			temp[ti * 2 + 1] = py;
		}
		cache = new int[(ti + 1) * 2];
		System.arraycopy(temp, 0, cache, 0, cache.length);
		if (forZoom <= 7)
			thickness = 0;
		else if (forZoom <= 14)
			thickness = 1;
		else
			thickness = 2;
	}

	public void Invalidate() {
		forZoom = -1;
	}

	public synchronized void Draw(Graphics g, MapCanvas map) {
		if (map.state.zoom != forZoom)
			Invalidate(map.state);

		final int cw = map.getWidth();
		final int ch = map.getHeight();
		final int px = start.GetScreenX(map.state);
		final int py = start.GetScreenY(map.state);

		for (int i = 2; i < cache.length; i += 2) {
			int x1 = px + cache[i - 2];
			int y1 = py + cache[i - 1];
			int x2 = px + cache[i];
			int y2 = py + cache[i + 1];
			boolean vis1 = Math.abs(x1) < cw && Math.abs(y1) < ch;
			boolean vis2 = Math.abs(x2) < cw && Math.abs(y2) < ch;
			if (!vis1 && !vis2)
				continue;
			g.setColor(0xff0000);
			if (thickness == 0) {
				g.drawLine(x1, y1, x2, y2);
			} else {
				// second point
				if (thickness == 1) {
					g.fillRect(x2 - 1, y2 - 1, 2, 2);
				} else {
					g.fillRect(x2 - (thickness >> 1), y2 - thickness, thickness, thickness << 1);
					g.fillRect(x2 - thickness, y2 - (thickness >> 1), thickness << 1, thickness);
				}
				// line
				{
					final int hor = Math.abs(x2 - x1);
					final int ver = Math.abs(y2 - y1);
					if (hor > ver) {
						g.fillTriangle(x1, y1 - thickness, x1, y1 + thickness, x2, y2 + thickness);
						g.fillTriangle(x1, y1 - thickness, x2, y2 - thickness, x2, y2 + thickness);
					} else {
						g.fillTriangle(x1 - thickness, y1, x1 + thickness, y1, x2 - thickness, y2);
						g.fillTriangle(x2 + thickness, y2, x1 + thickness, y1, x2 - thickness, y2);
					}
				}
			}
		}
	}

}

package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.screens.MapCanvas;
import mahomaps.ui.UIElement;

public final class OverlaysManager {

	public final static int OVERLAYS_SPACING = 11;

	private final Vector overlays = new Vector();
	private final MapCanvas map;
	public int overlaysH;
	private final Image dummyBuffer = Image.createImage(1, 1);

	public OverlaysManager(MapCanvas map) {
		this.map = map;
	}

	public void DrawMap(Graphics g, MapState ms) {
		for (int i = 0; i < overlays.size(); i++) {
			Vector points = ((MapOverlay) overlays.elementAt(i)).GetPoints();
			int s = points.size();
			for (int j = 0; j < s; j++) {
				((Geopoint) points.elementAt(j)).paint(g, ms);
			}
		}
	}

	public void Draw(Graphics g, int w, int h) {
		if (MahoMapsApp.GetCanvas().mapFocused && !UIElement.touchInput && overlays.size() > 0) {
			// there must be 1+ unfocused overlay and KB mode
			int fh = g.getFont().getHeight();
			int ch = overlays.size() * fh + 3;
			int y = h - ch + 3;
			g.setColor(MapOverlay.OVERLAY_BG);
			g.fillRoundRect(5, h - ch, w - 10, ch + 10, 10, 10);
			g.setColor(-1);
			for (int i = 0; i < overlays.size(); i++) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				mo.Paint(dummyBuffer.getGraphics(), 5, 0, w - 10, h); // for input queue processing
				g.drawString(MahoMapsApp.text[mo.GetName()], 11, y, 0);
				y += fh;
			}
			overlaysH = ch;
		} else {
			int y = h - overlaysH;
			int oh = 0;
			for (int i = 0; i < overlays.size(); i++) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				try {
					mo.Paint(g, 5, y, w - 10, h);
				} catch (Exception e) {
					throw new RuntimeException("Failed to paint overlay #" + i + " " + e.toString());
				}
				y += mo.H + OVERLAYS_SPACING;
				oh += mo.H + OVERLAYS_SPACING;
			}
			overlaysH = oh;
		}
	}

	public boolean OnGeopointTap(int x, int y) {
		synchronized (overlays) {
			for (int i = 0; i < overlays.size(); i++) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				Vector points = mo.GetPoints();
				int s = points.size();
				for (int j = 0; j < s; j++) {
					Geopoint p = (Geopoint) points.elementAt(j);
					if (p.isTouched(map, map.state, x, y)) {
						if (mo.OnPointTap(p)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void CloseOverlay(MapOverlay o) {
		CloseOverlay(o.GetId());
	}

	public void CloseOverlay(String id) {
		synchronized (overlays) {
			for (int i = overlays.size() - 1; i >= 0; i--) {
				if (((MapOverlay) overlays.elementAt(i)).GetId().equals(id))
					overlays.removeElementAt(i);
			}
			RecalcOverlaysHeight();
		}
		map.requestRepaint();
	}

	public void PushOverlay(MapOverlay o) {
		synchronized (overlays) {
			CloseOverlay(o.GetId());
			o.Paint(dummyBuffer.getGraphics(), 0, 0, map.getWidth(), map.getHeight());
			overlays.addElement(o);

			RecalcOverlaysHeight();
		}
	}

	public void RecalcOverlaysHeight() {
		int oh = 0;
		for (int i = 0; i < overlays.size(); i++) {
			MapOverlay mo = (MapOverlay) overlays.elementAt(i);
			oh += mo.H + OVERLAYS_SPACING;
		}

		overlaysH = oh;
	}

	public void InvalidateOverlayHeight(MapOverlay o) {
		synchronized (overlays) {
			o.Paint(dummyBuffer.getGraphics(), 0, 0, map.getWidth(), map.getHeight());
			RecalcOverlaysHeight();
		}
		map.requestRepaint();
	}

	public void InvalidateAllOverlaysHeight() {
		synchronized (overlays) {
			for (int i = 0; i < overlays.size(); i++) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				mo.Paint(dummyBuffer.getGraphics(), 0, 0, map.getWidth(), map.getHeight());
			}
			RecalcOverlaysHeight();
		}
		map.requestRepaint();
	}

	public MapOverlay GetOverlay(String id) {
		synchronized (overlays) {
			for (int i = overlays.size() - 1; i >= 0; i--) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				if (mo.GetId().equals(id))
					return mo;
			}
			return null;
		}
	}
}

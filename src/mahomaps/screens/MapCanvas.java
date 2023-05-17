package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.GeoUpdateThread;
import mahomaps.map.Geopoint;
import mahomaps.map.Rect;
import mahomaps.map.TileCache;
import mahomaps.map.TileId;
import mahomaps.map.TilesProvider;
import mahomaps.ui.MapOverlay;
import mahomaps.ui.UIComposite;

public class MapCanvas extends MultitouchCanvas {

	public final int buttonSize = 50;
	public final int buttonMargin = 10;

	private TilesProvider tiles;

	String[] buttons = new String[] { "geo", "-", "+", "menu" };

	private GeoUpdateThread geo = null;
	public final Geopoint geolocation;

	// STATE
	public int zoom = 0;
	public int tileX = 0;
	public int tileY = 0;
	public int xOffset = 0;
	public int yOffset = 0;
	int startPx, startPy;
	int lastPx, lastPy;
	public final Vector searchPoints = new Vector();
	public final Vector routePoints = new Vector();
	public MapOverlay overlay;
	private boolean touch = hasPointerEvents();

	public MapCanvas(TilesProvider tiles) {
		this.tiles = tiles;
		setFullScreenMode(true);
		geolocation = new Geopoint(0, 0);
		geolocation.type = Geopoint.LOCATION;
	}

	public void SetOverlayContent(UIComposite ui) {
		if (ui == null) {
			overlay = null;
			return;
		}
		MapOverlay o = new MapOverlay();
		o.X = 5;
		o.Y = 5;
		o.W = getWidth() - 10;
		o.content = ui;
		overlay = o;
	}

	// DRAW SECTION

	private void repaint(Graphics g) {
		final int w = getWidth();
		final int h = getHeight();
		g.setGrayScale(127);
		g.fillRect(0, 0, w, h);
		drawMap(g, w, h);
		drawOverlay(g, w, h);
		drawUi(g, w, h);
	}

	private void drawMap(Graphics g, int w, int h) {
		tiles.BeginMapPaint();
		g.translate(w >> 1, h >> 1);

		int trX = 1;
		while (trX * 256 < (w >> 1))
			trX++;
		int trY = 1;
		while (trY * 256 < (w >> 1))
			trY++;

		int y = yOffset - trY * 256;
		int yi = tileY - trY;
		while (y < h / 2) {
			int x = xOffset - trX * 256;
			int xi = tileX - trX;
			while (x < w / 2) {
				TileCache tile = tiles.getTile(new TileId(xi, yi, zoom));
				if (tile != null)
					tile.paint(g, x, y);
				x += 256;
				xi++;
			}
			y += 256;
			yi++;
		}

		for (int i = 0; i < searchPoints.size(); i++) {
			Geopoint p = (Geopoint) searchPoints.elementAt(i);
			p.paint(g, this);
		}
		for (int i = 0; i < routePoints.size(); i++) {
			Geopoint p = (Geopoint) routePoints.elementAt(i);
			p.paint(g, this);
		}
		if (geo != null && geo.state == GeoUpdateThread.STATE_OK) {
			geolocation.paint(g, this);
		}

		g.translate(-(w >> 1), -(h >> 1));
		tiles.EndMapPaint();
	}

	private void drawOverlay(Graphics g, int w, int h) {
		g.setColor(0);
		g.setFont(Font.getFont(0, 0, 8));
		if (Settings.drawTileInfo)
			g.drawString("x " + tileX + " y " + tileY + " zoom=" + zoom, 5, 5, 0);
		g.drawString(MahoMapsApp.api.token == null ? "no token" : "token OK", 5, 65, 0);

		if (geo != null) {
			g.drawString(GeoUpdateThread.states[geo.state], 5, 25, 0);
			if (geo.state == GeoUpdateThread.STATE_OK) {
				g.drawString(geolocation.lat + " " + geolocation.lon, 5, 45, 0);
			}
		}
		if (overlay != null)
			overlay.Paint(g, 0, 0, w, h);
	}

	private void drawUi(Graphics g, int w, int h) {
		int y = h;
		if (touch) {
			for (int i = 0; i < 4; i++) {
				y -= buttonSize;
				y -= buttonMargin;
				g.setGrayScale(220);
				g.fillArc(w - buttonSize - buttonMargin, y, buttonSize, buttonSize, 0, 360);
				if (i != 0) {
					g.setColor(0);
				} else {
					// geo
					if (geo == null) {
						g.setColor(0);
					} else {
						if (geo.state == GeoUpdateThread.STATE_OK) {
							g.setColor(0, 200, 0);
						} else if (geo.state == GeoUpdateThread.STATE_PENDING) {
							g.setColor(0, 0, 200);
						} else {
							g.setColor(255, 0, 0);
						}
					}
				}
				g.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
				g.drawString(buttons[i], w - buttonMargin - buttonSize / 2,
						y + buttonSize / 2 - g.getFont().getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
			}
		} else {
			Font f = Font.getFont(0, 0, 8);
			int fh = f.getHeight();
			g.setFont(f);
			g.setColor(0);
			g.fillRect(0, h - fh, w, fh);
			g.setColor(-1);
			String geos = geo == null ? "Геопозиция" : GeoUpdateThread.states[geo.state];
			g.drawString("Меню", 0, h - fh, 0);
			g.drawString("Выбор", w / 2, h - fh, Graphics.TOP | Graphics.HCENTER);
			g.drawString(geos, w, h - fh, Graphics.TOP | Graphics.RIGHT);
		}
	}

	// LOGIC

	public void update() {
		repaint(getGraphics());
		flushGraphics();
	}

	public void zoomIn() {
		if (zoom >= 18)
			return;
		zoom++;
		tileX *= 2;
		tileY *= 2;
		xOffset *= 2;
		yOffset *= 2;
		if (xOffset < -255) {
			tileX++;
			xOffset += 256;
		}
		if (yOffset < -255) {
			tileY++;
			yOffset += 256;
		}
	}

	public void zoomOut() {
		if (zoom <= 0)
			return;
		zoom--;
		tileX /= 2;
		tileY /= 2;
		xOffset /= 2;
		yOffset /= 2;
	}

	public void geo() {
		if (geo == null) {
			geo = new GeoUpdateThread(geolocation, this);
			geo.start();
		} else {
			geo.request();
		}
	}

	// INPUT

	protected void keyPressed(int k) {
		touch = false;
		if (k == -6)
			MahoMapsApp.BringMenu();
		if (k == -7)
			geo();
	}

	protected void keyReleased(int k) {

	}

	protected void pointerPressed(int x, int y, int n) {
		touch = true;
		if (n == 0) {
			startPx = x;
			startPy = y;
			lastPx = x;
			lastPy = y;
		}
	}

	protected void pointerDragged(int x, int y, int n) {
		if (n == 0) {
			xOffset += x - lastPx;
			yOffset += y - lastPy;
			lastPx = x;
			lastPy = y;

			if (xOffset > 0) {
				tileX--;
				xOffset -= 256;
			}
			if (yOffset > 0) {
				tileY--;
				yOffset -= 256;
			}
			if (xOffset < -255) {
				tileX++;
				xOffset += 256;
			}
			if (yOffset < -255) {
				tileY++;
				yOffset += 256;
			}
		}
	}

	protected void pointerReleased(int x, int y, int n) {
		if (n != 0)
			return;
		int w = getWidth();
		int h = getHeight();
		// TODO cache tap areas
		Rect menu = new Rect(w - buttonSize - buttonMargin, h - (buttonSize + buttonMargin) * 4, buttonSize,
				buttonSize);
		Rect plus = new Rect(w - buttonSize - buttonMargin, h - (buttonSize + buttonMargin) * 3, buttonSize,
				buttonSize);
		Rect minus = new Rect(w - buttonSize - buttonMargin, h - (buttonSize + buttonMargin) * 2, buttonSize,
				buttonSize);
		Rect geo = new Rect(w - buttonSize - buttonMargin, h - (buttonSize + buttonMargin), buttonSize, buttonSize);
		if (plus.containsBoth(x, y, startPx, startPy)) {
			zoomIn();
		} else if (minus.containsBoth(x, y, startPx, startPy)) {
			zoomOut();
		} else if (geo.containsBoth(x, y, startPx, startPy)) {
			geo();
		} else if (menu.containsBoth(x, y, startPx, startPy)) {
			MahoMapsApp.BringMenu();
		}
	}

	public void dispose() {
		if (geo != null)
			geo.interrupt();
	}

}

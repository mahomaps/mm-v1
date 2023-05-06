package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.*;

import java.util.Vector;

public class MapCanvas extends MultitouchCanvas implements CommandListener {

	public final int buttonSize = 50;
	public final int buttonMargin = 10;

	private TilesProvider tiles;
	private Command back = new Command("Назад", Command.BACK, 0);
	private Command routes = new Command("Маршрут", Command.ITEM, 1);
	private Command search = new Command("Поиск", Command.ITEM, 2);
	private Command settings = new Command("Настройки", Command.ITEM, 3);
	private Command about = new Command("О программе", Command.ITEM, 4);
	private Command moreapps = new Command("Другие программы", Command.ITEM, 5);

	String[] buttons = new String[] { "geo", "-", "+" };

	private GeoUpdateThread geo = null;
	private final Geopoint geolocation;

	// STATE
	public int zoom = 0;
	public int tileX = 0;
	public int tileY = 0;
	public int xOffset = 0;
	public int yOffset = 0;
	int startPx, startPy;
	int lastPx, lastPy;
	public final Vector points = new Vector();

	public MapCanvas(TilesProvider tiles) {
		this.tiles = tiles;
		addCommand(back);
		addCommand(routes);
		addCommand(search);
		addCommand(settings);
		addCommand(about);
		addCommand(moreapps);
		setCommandListener(this);
		geolocation = new Geopoint(0, 0);
		geolocation.type = Geopoint.LOCATION;
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

		for (int i = 0; i < points.size(); i++) {
			Geopoint p = (Geopoint) points.elementAt(i);
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
			g.drawString("x " + tileX + " y " + tileY + " z " + zoom, 5, 5, 0);

		if (!Settings.drawPointsInfo)
			return;

		int y = 20;
		for (int i = 0; i < points.size(); i++) {
			Geopoint p = (Geopoint) points.elementAt(i);
			g.drawString(p.GetScreenX(this) + " " + p.GetScreenY(this), 5, y, 0);
			y += 15;
		}
	}

	private void drawUi(Graphics g, int w, int h) {
		int y = h;
		for (int i = 0; i < 3; i++) {
			y -= buttonSize;
			y -= buttonMargin;
			g.setGrayScale(220);
			g.fillArc(w - buttonSize - buttonMargin, y, buttonSize, buttonSize, 0, 360);
			g.setColor(0);
			g.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
			g.drawString(buttons[i], w - buttonMargin - buttonSize / 2,
					y + buttonSize / 2 - g.getFont().getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
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
			// TODO bring position to center of screen
		}
	}

	// INPUT

	protected void keyPressed(int keyCode) {
	}

	protected void keyReleased(int keyCode) {
	}

	protected void pointerPressed(int x, int y, int n) {
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
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == about) {
			MahoMapsApp.BringSubScreen(new AboutScreen());
		} else if (c == moreapps) {
			MahoMapsApp.BringSubScreen(new OtherAppsScreen());
		}
	}

	public void dispose() {
		if (geo != null)
			geo.interrupt();
	}

}

package mahomaps.screens;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.api.YmapsApi;
import mahomaps.map.GeoUpdateThread;
import mahomaps.map.Geopoint;
import mahomaps.map.Route;
import mahomaps.map.TileCache;
import mahomaps.map.TileId;
import mahomaps.map.TilesProvider;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.ControlButtonsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.MapOverlay;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIComposite;
import mahomaps.ui.UIElement;

public class MapCanvas extends MultitouchCanvas implements IButtonHandler, CommandListener {

	public final int buttonSize = 50;
	public final int buttonMargin = 10;

	private TilesProvider tiles;

	String[] buttons = new String[] { "geo", "-", "+", "menu" };

	public GeoUpdateThread geo = null;

	// STATE
	public int zoom = 0;
	public int tileX = 0;
	public int tileY = 0;
	public int xOffset = 0;
	public int yOffset = 0;
	int startPx, startPy;
	int lastPx, lastPy;
	boolean dragActive;
	public final Vector searchPoints = new Vector();
	public final Vector routePoints = new Vector();
	public final Geopoint geolocation;
	public Geopoint selection;
	public MapOverlay overlay;
	public ControlButtonsContainer controls;
	private boolean touch = hasPointerEvents();
	private final Image dummyBuffer = Image.createImage(1, 1);

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command search = new Command("Поиск", Command.OK, 1);
	private TextBox searchBox = new TextBox("Поиск", "", 100, 0);

	public MapCanvas(TilesProvider tiles) {
		this.tiles = tiles;
		setFullScreenMode(true);
		geolocation = new Geopoint(0, 0);
		geolocation.type = Geopoint.LOCATION;

		searchBox.addCommand(back);
		searchBox.addCommand(search);
		searchBox.setCommandListener(this);

		controls = new ControlButtonsContainer(this);

		if (MahoMapsApp.api.token == null) {
			NotifyNullToken();
		}
	}

	private void NotifyNullToken() {
		SetOverlayContent(new FillFlowContainer(new UIElement[] { new SimpleText("Не удалось получить токен API.", 0),
				new SimpleText("Онлайн-функции будут недоступны.", 0), new Button("Ещё раз", -2, this, 5),
				new Button("Закрыть", 0, this, 5) }));
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
		// Рисуем в никуда для пересчёта макета (для обхода 1-кадрового мигания)
		// Почему бы не отделить макет от отрисовки? А зачем?
		o.Paint(dummyBuffer.getGraphics(), 0, 0, getWidth(), getHeight());
		overlay = o;
	}

	public Geopoint GetSearchAnchor() {
		if (geo != null && geo.DrawPoint()) {
			return geolocation;
		}
		return GetAtCoords(0, 0);
	}

	/**
	 * Получает точку на экране по координатам экрана.
	 *
	 * @param x X относительно центра (центр = 0)
	 * @param y Y относительно центра (центр = 0)
	 * @return Точка.
	 */
	public Geopoint GetAtCoords(int x, int y) {
		int tilesCount = 1 << zoom;
		double dx = x;
		dx -= xOffset;
		dx /= 256;
		dx += tileX;
		dx *= 360d;
		dx /= tilesCount;
		double lon = dx - 180d;

		Geopoint g = new Geopoint(0, lon);
		double step = 45d;
		while (step > 0.000001d) {
			double or = g.lat;
			g.lat = or + step;
			int plus = g.GetScreenY(this);
			g.lat = or - step;
			int minus = g.GetScreenY(this);
			plus -= y;
			minus -= y;
			if (Math.abs(plus - minus) <= 2) {
				g.lat = or;
				break;
			}
			if (Math.abs(plus) > Math.abs(minus)) {
				g.lat = or - step;
			} else {
				g.lat = or + step;
			}
			step /= 2d;
		}

		return g;
	}

	// DRAW SECTION

	private void repaint(Graphics g) {
		final int w = getWidth();
		final int h = getHeight();
		g.setGrayScale(127);
		g.fillRect(0, 0, w, h);
		drawMap(g, w, h);
		drawOverlay(g, w, h);
		UIElement.CommitInputQueue();
	}

	private void drawMap(Graphics g, int w, int h) {
		tiles.BeginMapPaint();
		g.translate(w >> 1, h >> 1);

		int trX = 1;
		while (trX * 256 < (w >> 1))
			trX++;
		int trY = 1;
		while (trY * 256 < (h >> 1))
			trY++;

		// кэширование для защиты от подмены переключенным потоком
		final int xo = xOffset;
		final int tx = tileX;

		int y = yOffset - trY * 256;
		int yi = tileY - trY;
		while (y < h / 2) {
			int x = xo - trX * 256;
			int xi = tx - trX;
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

		if (geo != null && geo.DrawPoint()) {
			geolocation.paint(g, this);
		}
		for (int i = 0; i < searchPoints.size(); i++) {
			Geopoint p = (Geopoint) searchPoints.elementAt(i);
			p.paint(g, this);
		}
		for (int i = 0; i < routePoints.size(); i++) {
			Geopoint p = (Geopoint) routePoints.elementAt(i);
			p.paint(g, this);
		}
		if (selection != null)
			selection.paint(g, this);

		g.translate(-(w >> 1), -(h >> 1));
		tiles.EndMapPaint();
	}

	private void drawOverlay(Graphics g, int w, int h) {
		g.setColor(0);
		g.setFont(Font.getFont(0, 0, 8));
		if (Settings.drawTileInfo)
			g.drawString("x " + tileX + " y " + tileY + " zoom=" + zoom, 5, 5, 0);

		if (geo != null) {
			g.drawString(GeoUpdateThread.states[geo.state], 5, 25, 0);
			if (geo.DrawPoint()) {
				String lat = String.valueOf(geolocation.lat);
				if (lat.length() > 4)
					lat = lat.substring(0, 4);
				String lon = String.valueOf(geolocation.lon);
				if (lon.length() > 4)
					lon = lon.substring(0, 4);
				g.drawString(lat + " " + lon, 5, 45, 0);
			}
		}
		if (overlay != null)
			overlay.Paint(g, 0, 0, w, h);
		if (controls != null)
			controls.Paint(g, 0, 0, w, h);
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
		} else if (geo.DrawPoint()) {
			zoom = 16;
			xOffset -= geolocation.GetScreenX(this);
			yOffset -= geolocation.GetScreenY(this);
			ClampOffset();
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
			UIElement.InvokePressEvent(x, y);
			dragActive = false;
			startPx = x;
			startPy = y;
			lastPx = x;
			lastPy = y;
		}
	}

	protected void pointerDragged(int x, int y, int n) {
		if (n == 0) {
			if (!dragActive) {
				if (Math.abs(x - startPx) > 8 || Math.abs(y - startPy) > 8) {
					UIElement.InvokeReleaseEvent();
					dragActive = true;
				} else
					return;
			}
			xOffset += x - lastPx;
			yOffset += y - lastPy;
			lastPx = x;
			lastPy = y;

			ClampOffset();
		}
	}

	private void ClampOffset() {
		while (xOffset > 0) {
			tileX--;
			xOffset -= 256;
		}
		while (yOffset > 0) {
			tileY--;
			yOffset -= 256;
		}
		while (xOffset < -255) {
			tileX++;
			xOffset += 256;
		}
		while (yOffset < -255) {
			tileY++;
			yOffset += 256;
		}
	}

	protected void pointerReleased(int x, int y, int n) {
		if (n != 0)
			return;
		if (dragActive)
			return;

		UIElement.InvokeReleaseEvent();
		if (UIElement.InvokeTouchEvent(x, y))
			return;

		// points

		if (MahoMapsApp.lastSearch != null) {
			Vector v = searchPoints;
			for (int i = v.size() - 1; i >= 0; i--) {
				Geopoint p = (Geopoint) v.elementAt(i);
				if (p.color == Geopoint.COLOR_RED)
					continue;
				if (p.isTouched(this, x, y)) {
					if (p.object != null) {
						new SearchResultScreen((JSONObject) p.object).BringAtMap();
						return;
					}
				}
			}
		}

		// tap at map
		if (MahoMapsApp.lastSearch == null) {
			selection = GetAtCoords(x - getWidth() / 2, y - getHeight() / 2);
			selection.color = Geopoint.COLOR_RED;
			selection.type = Geopoint.POI_SELECT;

			SetOverlayContent(new FillFlowContainer(new UIElement[] { new SimpleText(selection.toString(), 0),
					new Button("Что здесь?", 6, this, 5),
					new ColumnsContainer(
							new UIElement[] { new Button("Точка А", 7, this, 5), new Button("Точка Б", 8, this, 5) }),
					new Button("Закрыть", 0, this, 5) }));
		}
	}

	public void dispose() {
		if (geo != null)
			geo.Dispose();
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case -2:
			MahoMapsApp.BringSubScreen(new APIReconnectForm());
			SetOverlayContent(null);
			break;
		case 0:
			selection = null;
			SetOverlayContent(null);
			break;
		case 1:
			if (MahoMapsApp.api.token == null) {
				NotifyNullToken();
			} else if (MahoMapsApp.lastSearch != null) {
				MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
			} else {
				MahoMapsApp.BringSubScreen(searchBox);
			}
			break;
		case 2:
			MahoMapsApp.BringMenu();
			break;
		case 3:
			zoomIn();
			break;
		case 4:
			zoomOut();
			break;
		case 5:
			geo();
			break;
		case 6:
			MahoMapsApp.BringSubScreen(new SearchScreen(selection.toString(), selection));
			break;
		case 7:
			PushRoutePoint(selection, 1);
			break;
		case 8:
			PushRoutePoint(selection, 2);
			break;
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == searchBox) {
			if (c == back) {
				MahoMapsApp.BringMap();
			} else {
				Geopoint geo = GetSearchAnchor();
				MahoMapsApp.BringSubScreen(new SearchScreen(searchBox.getString(), geo));
			}
		}
	}

	private void PushRoutePoint(Geopoint geo, int side) {
		if (MahoMapsApp.api.token == null) {
			NotifyNullToken();
			return;
		}
		Geopoint a = null;
		Geopoint b = null;
		boolean set = false;
		for (int i = 0; i < routePoints.size(); i++) {
			Geopoint p = (Geopoint) routePoints.elementAt(i);
			if (p.type == Geopoint.ROUTE_A)
				a = p;
			if (p.type == Geopoint.ROUTE_B)
				b = p;

			if (side == 1 && p.type == Geopoint.ROUTE_A) {
				p.lat = geo.lat;
				p.lon = geo.lon;
				set = true;
			}
			if (side == 2 && p.type == Geopoint.ROUTE_B) {
				p.lat = geo.lat;
				p.lon = geo.lon;
				set = true;
			}
		}
		if (!set) {
			Geopoint p = new Geopoint(geo.lat, geo.lon);
			p.type = side == 1 ? Geopoint.ROUTE_A : Geopoint.ROUTE_B;
			p.color = Geopoint.COLOR_BLUE;
			if (side == 1)
				a = p;
			else
				b = p;
			routePoints.addElement(p);
		}

		if (a != null && b != null) {
			try {
				Geopoint[] res = new Route(MahoMapsApp.api.Route(a, b, YmapsApi.ROUTE_BYFOOT)).points;
				for (int i = 0; i < res.length; i++) {
					routePoints.addElement(res[i]);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}

package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.GeoUpdateThread;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.map.TileCache;
import mahomaps.map.TileId;
import mahomaps.map.TilesProvider;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.NoApiTokenOverlay;
import mahomaps.overlays.SelectOverlay;
import mahomaps.ui.ControlButtonsContainer;
import mahomaps.ui.UIElement;

public class MapCanvas extends MultitouchCanvas implements CommandListener {

	public final int buttonSize = 50;
	public final int buttonMargin = 10;

	private TilesProvider tiles;

	String[] buttons = new String[] { "geo", "-", "+", "menu" };

	public GeoUpdateThread geo = null;

	// STATE
	public volatile MapState state = MapState.Default();
	int startPx, startPy;
	int lastPx, lastPy;
	boolean dragActive;
	private final Vector overlays = new Vector();
	private int overlaysH;
	public final Geopoint geolocation;
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

		CheckApiAcsess();
	}

	/**
	 * Проверяет есть ли доступ к апи. Выводит окно предупреждения.
	 * 
	 * @return False если нету.
	 */
	public boolean CheckApiAcsess() {
		if (MahoMapsApp.api.token == null) {
			PushOverlay(new NoApiTokenOverlay());
			return false;
		}
		return true;
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
		}
	}

	public void PushOverlay(MapOverlay o) {
		synchronized (overlays) {
			CloseOverlay(o.GetId());
			// Рисуем в никуда для пересчёта макета (для обхода 1-кадрового мигания)
			// Почему бы не отделить макет от отрисовки? А зачем?
			o.Paint(dummyBuffer.getGraphics(), 0, 0, getWidth(), getHeight());
			overlays.addElement(o);
		}
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

	public Geopoint GetSearchAnchor() {
		if (geo != null && geo.DrawPoint()) {
			return geolocation;
		}
		Geopoint p = GetAtCoords(0, 0);
		if (p.lat > 80d)
			p.lat = 80d;
		if (p.lat < -80d)
			p.lat = -80d;
		return p;
	}

	/**
	 * Получает точку на экране по координатам экрана.
	 *
	 * @param x X относительно центра (центр = 0)
	 * @param y Y относительно центра (центр = 0)
	 * @return Точка.
	 */
	public Geopoint GetAtCoords(int x, int y) {
		MapState ms = state.Clone();
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

	// DRAW SECTION

	private void repaint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		g.setGrayScale(127);
		g.fillRect(0, 0, w, h);
		drawMap(g, w, h);
		drawOverlay(g, w, h);
		UIElement.CommitInputQueue();
	}

	private void drawMap(Graphics g, int w, int h) {
		MapState ms = state;
		tiles.BeginMapPaint();
		g.translate(w >> 1, h >> 1);

		int trX = 1;
		while (trX * 256 < (w >> 1))
			trX++;
		int trY = 1;
		while (trY * 256 < (h >> 1))
			trY++;

		// кэширование для защиты от подмены переключенным потоком
		final int xo = ms.xOffset;
		final int tx = ms.tileX;

		int y = ms.yOffset - trY * 256;
		int yi = ms.tileY - trY;
		while (y < h / 2) {
			int x = xo - trX * 256;
			int xi = tx - trX;
			while (x < w / 2) {
				TileCache tile = tiles.getTile(new TileId(xi, yi, ms.zoom));
				if (tile != null)
					tile.paint(g, x, y);
				x += 256;
				xi++;
			}
			y += 256;
			yi++;
		}

		if (geo != null && geo.DrawPoint()) {
			geolocation.paint(g, ms);
		}

		for (int i = 0; i < overlays.size(); i++) {
			Vector points = ((MapOverlay) overlays.elementAt(i)).GetPoints();
			int s = points.size();
			for (int j = 0; j < s; j++) {
				((Geopoint) points.elementAt(j)).paint(g, ms);
			}
		}

		g.translate(-(w >> 1), -(h >> 1));
		tiles.EndMapPaint();
	}

	private void drawOverlay(Graphics g, int w, int h) {
		g.setColor(0);
		g.setFont(Font.getFont(0, 0, 8));
		if (Settings.drawTileInfo)
			g.drawString(state.toString(), 5, 5, 0);

		if (geo != null && Settings.showGeo) {
			g.drawString(GeoUpdateThread.states[geo.state] + " " + geo.sattelites + " " + geo.method, 5, 25, 0);
			if (geo.DrawPoint()) {
				g.drawString(geolocation.toString(), 5, 45, 0);
			}
		}

		int y = h - overlaysH;
		int oh = 0;
		for (int i = 0; i < overlays.size(); i++) {
			MapOverlay mo = (MapOverlay) overlays.elementAt(i);
			mo.Paint(g, 5, y, w - 10, h);
			y += mo.H + 15;
			oh += mo.H + 15;
		}

		if (controls != null)
			controls.Paint(g, 0, 0, w, h - overlaysH);

		overlaysH = oh;
	}

	// LOGIC

	public void update() {
		repaint(getGraphics());
		flushGraphics();
	}

	public void ShowGeo() {
		if (geo == null) {
			geo = new GeoUpdateThread(geolocation, this);
			geo.start();
		} else if (geo.DrawPoint()) {
			state = MapState.FocusAt(geolocation);
		} else if (geo.state == GeoUpdateThread.STATE_UNAVAILABLE) {
			geo.restart();
		}
	}

	// INPUT

	protected void keyPressed(int k) {
		touch = false;
		if (k == -6)
			MahoMapsApp.BringMenu();
		if (k == -7)
			ShowGeo();
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
			state.xOffset += x - lastPx;
			state.yOffset += y - lastPy;
			lastPx = x;
			lastPy = y;

			state.ClampOffset();
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

		synchronized (overlays) {
			for (int i = 0; i < overlays.size(); i++) {
				MapOverlay mo = (MapOverlay) overlays.elementAt(i);
				Vector points = mo.GetPoints();
				int s = points.size();
				for (int j = 0; j < s; j++) {
					Geopoint p = (Geopoint) points.elementAt(j);
					if (p.isTouched(this, state, x, y)) {
						if (mo.OnPointTap(p))
							return;
					}
				}
			}
		}

		// tap at map
		if (MahoMapsApp.lastSearch == null) {
			Geopoint s = GetAtCoords(x - getWidth() / 2, y - getHeight() / 2);
			if (Math.abs(s.lat) > 85) {
				return;
			}
			PushOverlay(new SelectOverlay(s));
		}
	}

	public void dispose() {
		if (geo != null)
			geo.Dispose();
	}

	public void BeginTextSearch() {
		if (CheckApiAcsess()) {
			if (MahoMapsApp.lastSearch != null) {
				throw new IllegalStateException("Can't begin new search while old one is open!");
			}

			MahoMapsApp.BringSubScreen(searchBox);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == searchBox) {
			if (c == back) {
				MahoMapsApp.BringMap();
			} else {
				CloseOverlay(SelectOverlay.ID);
				Geopoint sa = GetSearchAnchor();
				MahoMapsApp.BringSubScreen(new SearchScreen(searchBox.getString(), sa));
			}
		}
	}
/*
	private void PushRoutePoint(Geopoint geo, int side) {
		if (!CheckApiAcsess())
			return;
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
	}*/

}

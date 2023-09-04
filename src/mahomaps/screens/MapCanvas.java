package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;

import mahomaps.FpsLimiter;
import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.GeoUpdateThread;
import mahomaps.map.Geopoint;
import mahomaps.map.Line;
import mahomaps.map.MapState;
import mahomaps.map.TileCache;
import mahomaps.map.TileId;
import mahomaps.map.TilesProvider;
import mahomaps.overlays.NoApiTokenOverlay;
import mahomaps.overlays.OverlaysManager;
import mahomaps.overlays.SelectOverlay;
import mahomaps.overlays.TileCacheForbiddenOverlay;
import mahomaps.overlays.TileDownloadForbiddenOverlay;
import mahomaps.route.RouteTracker;
import mahomaps.ui.ControlButtonsContainer;
import mahomaps.ui.UIElement;

public class MapCanvas extends MultitouchCanvas implements CommandListener {

	public volatile MapState state = Settings.ReadStateOrDefault();

	// search lcdui parts
	private Command search = new Command(MahoMapsApp.text[27], Command.OK, 1);
	private TextBox searchBox = new TextBox(MahoMapsApp.text[27], "", 100, 0);

	// geolocation stuff
	public GeoUpdateThread geo = null;
	public Geopoint geolocation;

	// input states
	private boolean mapFocused = true;
	private int repeatCount = 0;
	int startPx, startPy;
	int lastPx, lastPy;

	// tiles/overlays stuff
	private final TilesProvider tiles;
	public final ControlButtonsContainer controls;
	public final OverlaysManager overlays = new OverlaysManager(this);
	public Line line;

	// draw/input states/temps/caches
	boolean dragActive;
	public boolean hidden = false;
	private int lastOverlaysW;
	public final FpsLimiter repaintGate = new FpsLimiter();
	private Graphics cachedGraphics;
	/*
	 * up down left right (1<< 0 1 2 3)
	 */
	private int keysState = 0;
	private Thread repeatThread;
	private final Runnable repeatAction = new Runnable() {
		public void run() {
			try {
				Thread.sleep(200); // wait a little before repeats
				while (true) {
					Thread.sleep(16); // interruption will throw here stopping the thread
					if (!mapFocused) {
						synchronized (repeatAction) {
							repeatThread = null;
							return;
						}
					}
					int val = Math.min(20, repeatCount / 3);
					if ((keysState & 1) != 0)
						state.yOffset += val;
					if ((keysState & 2) != 0)
						state.yOffset -= val;
					if ((keysState & 4) != 0)
						state.xOffset += val;
					if ((keysState & 8) != 0)
						state.xOffset -= val;
					state.ClampOffset();
					repeatCount++;
					requestRepaint();
				}
			} catch (InterruptedException e) {
			}
		}
	};

	public MapCanvas(TilesProvider tiles) {
		this.tiles = tiles;
		setFullScreenMode(true);
		geolocation = new Geopoint(0, 0);
		geolocation.type = Geopoint.LOCATION;

		searchBox.addCommand(MahoMapsApp.back);
		searchBox.addCommand(search);
		searchBox.setCommandListener(this);

		UIElement.touchInput = hasPointerEvents();

		controls = new ControlButtonsContainer(this);

		CheckApiAcsess();
		if (Settings.cacheMode == Settings.CACHE_DISABLED)
			overlays.PushOverlay(new TileCacheForbiddenOverlay());
		if (!Settings.allowDownload)
			overlays.PushOverlay(new TileDownloadForbiddenOverlay());
	}

	/**
	 * Проверяет есть ли доступ к апи. Выводит окно предупреждения.
	 * 
	 * @return False если нету.
	 */
	public boolean CheckApiAcsess() {
		if (MahoMapsApp.api.token == null) {
			overlays.PushOverlay(new NoApiTokenOverlay());
			return false;
		}
		return true;
	}

	public Geopoint GetSearchAnchor() {
		if (geo != null && geo.DrawPoint()) {
			return geolocation;
		}
		Geopoint p = Geopoint.GetAtCoords(state, 0, 0);
		if (p.lat > 80d)
			p.lat = 80d;
		if (p.lat < -80d)
			p.lat = -80d;
		return p;
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
		if (UIElement.IsQueueEmpty() && !UIElement.touchInput && !mapFocused) {
			mapFocused = true;
			UIElement.Deselect();
			requestRepaint();
		}
	}

	private void drawMap(Graphics g, int w, int h) {
		MapState ms = state;
		if (ms == null) // wtf!?
			throw new NullPointerException("Map state was null at frame begin");

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
				TileCache tile = tiles.getTile(new TileId(xi, yi, ms.zoom, Settings.map));
				if (tile != null)
					tile.paint(g, x, y);
				x += 256;
				xi++;
			}
			y += 256;
			yi++;
		}

		Line l = line;
		if (l != null)
			l.Draw(g, this);

		if (geo != null && geo.DrawPoint()) {
			geolocation.paint(g, ms);
		}

		if (!UIElement.touchInput) {
			g.setColor(0xff0000);

			g.drawLine(-6, 0, -2, 0);
			g.drawLine(2, 0, 6, 0);
			g.drawLine(0, -6, 0, -2);
			g.drawLine(0, 2, 0, 6);
		}

		overlays.DrawMap(g, ms);

		g.translate(-(w >> 1), -(h >> 1));
		tiles.EndMapPaint(state);
	}

	private void drawOverlay(Graphics g, int w, int h) {
		Font f = Font.getFont(0, 0, 8);
		g.setColor(0);
		g.setFont(f);
		if (Settings.drawDebugInfo)
			g.drawString(state.toString(), 0, 0, 0);

		try {
			controls.info = GetGeoInfo();
		} catch (Exception e) {
			// wtf!!!???
			throw new RuntimeException("geo infa ebanulas " + e.toString());
		}

		final boolean t = UIElement.touchInput;
		final RouteTracker rt = MahoMapsApp.route;

		int fh = f.getHeight();
		if (!t)
			h -= fh;

		lastOverlaysW = getOverlaysW(w, h);

		if (t || rt == null)
			overlays.Draw(g, lastOverlaysW, h);

		if (t) {
			int ch = h;
			if (lastOverlaysW == w)
				ch -= overlays.overlaysH;
			controls.Paint(g, 0, 0, w, ch);
		}
		controls.PaintInfo(g, 0, 0, w, h - overlays.overlaysH);

		if (rt != null) {
			mapFocused = true;
			rt.Update();
			rt.Draw(g, w);
		}

		if (!t) {
			g.setColor(0);
			g.fillRect(0, h, w, fh);
			g.setColor(-1);
			g.setFont(f);

			if (rt == null) {
				g.drawString(MahoMapsApp.text[28], 0, h, 0);
				g.drawString(MahoMapsApp.text[29], w / 2, h, Graphics.TOP | Graphics.HCENTER);

				if (mapFocused) {
					if (!UIElement.IsQueueEmpty())
						g.drawString(MahoMapsApp.text[30], w, h, Graphics.TOP | Graphics.RIGHT);
				} else {
					g.drawString(MahoMapsApp.text[31], w, h, Graphics.TOP | Graphics.RIGHT);
				}
			} else {
				g.drawString(MahoMapsApp.text[32], w, h, Graphics.TOP | Graphics.RIGHT);
			}
		}
	}

	private Vector GetGeoInfo() {
		if (geo == null || Settings.showGeo == 0) {
			return null;
		}

		Vector v = new Vector();

		if (geo.state == GeoUpdateThread.STATE_UNSUPPORTED) {
			v.addElement(GeoUpdateThread.states[geo.state]);
			return v;
		}

		// статус и время
		int passed = (int) ((System.currentTimeMillis() - geo.lastUpdateTime) / 1000);
		if (geo.state != GeoUpdateThread.STATE_OK) {
			v.addElement(GeoUpdateThread.states[geo.state]);
			v.addElement(MahoMapsApp.text[83] + passed + MahoMapsApp.text[84]);
		} else if (passed >= 5) {
			v.addElement(MahoMapsApp.text[85] + passed + MahoMapsApp.text[86]);
		}

		// метод и спутники
		{
			String s = geo.method;
			if (geo.totalSattelitesInView >= 0) {
				String s2 = (geo.sattelites >= 0 ? geo.sattelites + "/" : "") + geo.totalSattelitesInView;
				if (s == null) {
					s = MahoMapsApp.text[87] + ": " + s2;
				} else {
					s += " (" + s2 + ")";
				}
			}
			if (s != null)
				v.addElement(s);
		}

		// координаты
		if (geo.DrawPoint() && Settings.showGeo == 2) {
			String[] r = geolocation.GetRounded();
			v.addElement(r[0]);
			v.addElement(r[1]);
		}
		return v;
	}

	private static int getOverlaysW(int w, int h) {
		if (w <= h) {
			// portait
			return w;
		}

		if (h + 100 > w) {
			// album, but too square (less than 100 pixels free)
			return w;
		}

		// album, wide enough
		return h;
	}

	public void run() throws InterruptedException {
		tryStartRepeatThread();
		while (true) {
			final RouteTracker rt = MahoMapsApp.route;
			if (MahoMapsApp.paused || hidden) {
				if (rt == null) {
					// we are hidden
					repaintGate.Pass();
				} else {
					// we are hidden, but route is active
					repaintGate.Begin();
					rt.Update();
					// drawing does nothing
					repaintGate.End(50); // 20 ups
				}
			} else {
				// we are visible
				repaintGate.Begin();
				Graphics g = cachedGraphics;
				if (g == null)
					cachedGraphics = g = getGraphics();
				repaint(g);
				flushGraphics();
				repaintGate.End(rt != null ? 33 : 2000);
			}
		}
	}

	public void requestRepaint() {
		repaintGate.Reset();
	}

	public void ShowGeo() {
		if (MahoMapsApp.route != null)
			return;
		if (geo == null) {
			geo = new GeoUpdateThread(geolocation, this);
			geo.start();
			Settings.PushUsageFlag(8);
		} else if (geo.DrawPoint()) {
			state = MapState.FocusAt(geolocation);
		} else if (geo.state == GeoUpdateThread.STATE_UNAVAILABLE) {
			geo.restart();
		}

		requestRepaint();
	}

	// INPUT

	protected void keyPressed(int k) {
		// "home" button
		if (k == -12)
			return;

		UIElement.touchInput = false;

		if (MahoMapsApp.route != null) {
			// когда маршрут ведётся, можно только изменять масштаб и закрывать маршрут.
			switch (k) {
			case -7:
			case -22:
				MahoMapsApp.route.overlay.OnButtonTap(null, 0);
				break;
			case KEY_NUM1:
			case KEY_STAR:
				state = state.ZoomOut();
				break;
			case KEY_NUM3:
			case KEY_POUND:
				state = state.ZoomIn();
				break;
			}
			requestRepaint();
			return;
		}

		if (k == -6 || k == -21) { // -21 и -22 для моторол
			MahoMapsApp.BringMenu();
			return;
		}
		if (k == -7 || k == -22) {
			tryStopRepeatThread(true);
			if (mapFocused) {
				if (!UIElement.IsQueueEmpty()) {
					mapFocused = false;
					UIElement.SelectDown();
				}
			} else {
				mapFocused = true;
				UIElement.Deselect();
			}
			requestRepaint();
			return;
		}
		handling: {
			int ga = 0;
			try {
				ga = getGameAction(k);
			} catch (IllegalArgumentException e) { // j2l moment
			}
			if (mapFocused) {
				switch (ga) {
				case FIRE:
					tryStopRepeatThread(true);
					Geopoint s = Geopoint.GetAtCoords(state, 0, 0);
					if (s.IsValid() && MahoMapsApp.lastSearch == null) {
						// немного костылей:
						// сбрасываем ввод
						UIElement.CommitInputQueue();
						// добавляем оверлей, он переотрисовывается заполняя очередь ввода
						overlays.PushOverlay(new SelectOverlay(s));
						// применяем очередь
						UIElement.CommitInputQueue();
						// снятие фокуса с карты
						mapFocused = false;
						// выбор кнопки
						UIElement.SelectDown();
						// после нажатия кнопки канва перерисует себя, вернув очередь ввода в адекватное
						// состояние
					}
					break handling;
				case UP:
					keysState |= 1 << 0;
					state.yOffset += 10;
					state.ClampOffset();
					tryStartRepeatThread();
					break handling;
				case DOWN:
					keysState |= 1 << 1;
					state.yOffset -= 10;
					state.ClampOffset();
					tryStartRepeatThread();
					break handling;
				case LEFT:
					keysState |= 1 << 2;
					state.xOffset += 10;
					state.ClampOffset();
					tryStartRepeatThread();
					break handling;
				case RIGHT:
					keysState |= 1 << 3;
					state.xOffset -= 10;
					state.ClampOffset();
					tryStartRepeatThread();
					break handling;
				}
				switch (k) {
				case KEY_NUM1:
					ShowGeo();
					break handling;
				case KEY_NUM3:
					// geo status toggle
					Settings.showGeo++;
					if (Settings.showGeo > 2)
						Settings.showGeo = 0;
					break handling;
				case KEY_NUM7:
					BeginTextSearch();
					break handling;
				case KEY_NUM9:
					MahoMapsApp.BringSubScreen(new BookmarksScreen());
					break handling;
				case KEY_STAR:
					state = state.ZoomIn();
					break handling;
				case KEY_POUND:
					state = state.ZoomOut();
					break handling;
				case KEY_NUM0:
					// layer toggle
					MahoMapsApp.BringSubScreen(new MapLayerSelectionScreen());
					break handling;
				}
			} else {
				switch (ga) {
				case FIRE:
					UIElement.TriggerSelected();
					break;
				case UP:
				case LEFT:
					UIElement.SelectUp();
					break;
				case DOWN:
				case RIGHT:
					UIElement.SelectDown();
					break;
				}
			}
		}
		requestRepaint();
	}

	protected void keyReleased(int k) {
		int ga = 0;
		try {
			ga = getGameAction(k);
		} catch (IllegalArgumentException e) {
		}
		switch (ga) {
		case UP:
			keysState &= ~(1 << 0);
			tryStopRepeatThread(false);
			break;
		case DOWN:
			keysState &= ~(1 << 1);
			tryStopRepeatThread(false);
			break;
		case LEFT:
			keysState &= ~(1 << 2);
			tryStopRepeatThread(false);
			break;
		case RIGHT:
			keysState &= ~(1 << 3);
			tryStopRepeatThread(false);
			break;
		}
		requestRepaint();
	}

	protected void pointerPressed(int x, int y, int n) {
		UIElement.touchInput = true;
		mapFocused = true;
		if (n == 0) {
			UIElement.InvokePressEvent(x, y);
			dragActive = false;
			startPx = x;
			startPy = y;
			lastPx = x;
			lastPy = y;
		}
		requestRepaint();
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

			if (MahoMapsApp.route != null)
				return;

			state.xOffset += x - lastPx;
			state.yOffset += y - lastPy;
			lastPx = x;
			lastPy = y;

			state.ClampOffset();
		}
		requestRepaint();
	}

	protected void pointerReleased(int x, int y, int n) {
		if (n != 0)
			return;
		handling: {
			if (dragActive) {
				dragActive = false;
				break handling;
			}

			UIElement.InvokeReleaseEvent();
			if (UIElement.InvokeTouchEvent(x, y))
				break handling;

			if (y > getHeight() - overlays.overlaysH && x < lastOverlaysW)
				break handling;

			if (overlays.OnGeopointTap(x, y))
				break handling;

			// tap at map
			if (MahoMapsApp.lastSearch == null && MahoMapsApp.route == null) {
				Geopoint s = Geopoint.GetAtCoords(state, x - getWidth() / 2, y - getHeight() / 2);
				if (s.IsValid()) {
					overlays.PushOverlay(new SelectOverlay(s));
					break handling;
				}
			}

		}
		requestRepaint();
	}

	public void dispose() {
		if (geo != null)
			geo.Dispose();
		Settings.SaveState(state);
	}

	public void BeginTextSearch() {
		if (MahoMapsApp.route != null)
			return;
		if (CheckApiAcsess()) {
			if (MahoMapsApp.lastSearch == null) {
				MahoMapsApp.BringSubScreen(searchBox);
			}
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == searchBox) {
			if (c == MahoMapsApp.back) {
				MahoMapsApp.BringMap();
			} else {
				overlays.CloseOverlay(SelectOverlay.ID);
				Geopoint sa = GetSearchAnchor();
				MahoMapsApp.BringSubScreen(new SearchLoader(searchBox.getString(), sa));
				Settings.PushUsageFlag(16);
			}
		}
	}

	protected void sizeChanged(int w, int h) {
		cachedGraphics = null;
		super.sizeChanged(w, h);
		requestRepaint();
	}

	protected void hideNotify() {
		hidden = true;
		tryStopRepeatThread(true);
	}

	protected void showNotify() {
		cachedGraphics = null;
		hidden = false;
		super.showNotify();
		requestRepaint();
	}

	private final void tryStartRepeatThread() {
		synchronized (repeatAction) {
			if (keysState != 0 && repeatThread == null) {
				Thread t = new Thread(repeatAction, "Key repeat");
				repeatCount = 0;
				t.start();
				repeatThread = t;
			}
		}
	}

	private final void tryStopRepeatThread(boolean force) {
		synchronized (repeatAction) {
			if (keysState == 0 || force) {
				Thread t = repeatThread;
				if (t != null) {
					t.interrupt();
					repeatThread = null;
				}
			}
		}
	}
}

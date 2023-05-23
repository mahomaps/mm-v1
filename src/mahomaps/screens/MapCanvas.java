package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;

import mahomaps.Gate;
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
import mahomaps.ui.ControlButtonsContainer;
import mahomaps.ui.UIElement;

public class MapCanvas extends MultitouchCanvas implements CommandListener {

	public volatile MapState state = MapState.Default();

	// search lcdui parts
	private Command back = new Command("Назад", Command.BACK, 0);
	private Command search = new Command("Поиск", Command.OK, 1);
	private TextBox searchBox = new TextBox("Поиск", "", 100, 0);

	// geolocation stuff
	public GeoUpdateThread geo = null;
	public final Geopoint geolocation;

	// input states
	private boolean touch = hasPointerEvents();
	private boolean mapFocused = true;
	private int repeatCount = 0;
	int startPx, startPy;
	int lastPx, lastPy;

	// tiles/overlays stuff
	private final TilesProvider tiles;
	public final ControlButtonsContainer controls;
	public final OverlaysManager overlays = new OverlaysManager(this);
	public Line line;

	// misc
	boolean dragActive;
	private boolean repaintDebugTick = true;
	public boolean hidden = false;
	public final Gate repaintGate = new Gate(true);
	private Graphics cachedGraphics;

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
		if (UIElement.IsQueueEmpty() && !touch && !mapFocused) {
			mapFocused = true;
			UIElement.Deselect();
			requestRepaint();
		}
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

		Line l = line;
		if (l != null)
			l.Draw(g, this);

		if (geo != null && geo.DrawPoint()) {
			geolocation.paint(g, ms);
		}

		overlays.DrawMap(g, ms);

		g.translate(-(w >> 1), -(h >> 1));
		tiles.EndMapPaint(state);
	}

	private void drawOverlay(Graphics g, int w, int h) {
		Font f = Font.getFont(0, 0, 8);
		g.setColor(0);
		g.setFont(f);
		if (Settings.drawTileInfo)
			g.drawString(state.toString(), 5, 5, 0);

		controls.info = GetGeoInfo();

		boolean t = touch;
		int fh = f.getHeight();
		if (!t)
			h -= fh;

		overlays.Draw(g, w, h);

		if (t) {
			controls.Paint(g, 0, 0, w, h - overlays.overlaysH);
		}
		controls.PaintInfo(g, 0, 0, w, h - overlays.overlaysH);

		if (!t) {
			g.setColor(0);
			g.fillRect(0, h, w, fh);
			g.setColor(-1);
			g.setFont(f);

			g.drawString("Меню", 0, h, 0);
			g.drawString("Выбор", w / 2, h, Graphics.TOP | Graphics.HCENTER);
			if (mapFocused) {
				if (!UIElement.IsQueueEmpty())
					g.drawString("К панелям", w, h, Graphics.TOP | Graphics.RIGHT);
			} else {
				g.drawString("К карте", w, h, Graphics.TOP | Graphics.RIGHT);
			}
		}
	}

	private Vector GetGeoInfo() {
		if (geo == null || Settings.showGeo == 0) {
			return null;
		}

		Vector v = new Vector();

		// статус и время
		int passed = (int) ((System.currentTimeMillis() - geo.lastUpdateTime) / 1000);
		if (geo.state != GeoUpdateThread.STATE_OK) {
			v.addElement(GeoUpdateThread.states[geo.state]);
			v.addElement("Прошло " + passed + " с.");
		} else if (passed >= 5) {
			v.addElement("Не обновлялось: " + passed + " с.");
		}

		// метод и спутники
		{
			String s = geo.method;
			if (geo.totalSattelitesInView >= 0) {
				String s2 = (geo.sattelites >= 0 ? geo.sattelites + "/" : "") + geo.totalSattelitesInView;
				if (s == null) {
					s = "Спутников: " + s2;
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

	public void run() throws InterruptedException {
		while (true) {
			if (MahoMapsApp.paused || hidden) {
				repaintGate.Pass();
			}
			{
				long time = System.currentTimeMillis();
				Graphics g = cachedGraphics;
				if (g == null)
					cachedGraphics = g = getGraphics();
				repaint(g);
				time = System.currentTimeMillis() - time;
				g.setColor(0);
				g.drawString(time + "ms " + (repaintDebugTick ? "+" : "="), 5, 65, 0);
				flushGraphics();
				repaintDebugTick = !repaintDebugTick;
			}
			repaintGate.Pass(2000);
		}
	}

	public void requestRepaint() {
		repaintGate.Reset();
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

		requestRepaint();
	}

	// INPUT

	protected void keyPressed(int k) {
		// "home" button
		if (k == -12)
			return;

		touch = false;
		if (k == -6) {
			MahoMapsApp.BringMenu();
			return;
		}
		if (k == -7) {
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
					state.yOffset += 10;
					state.ClampOffset();
					break handling;
				case DOWN:
					state.yOffset -= 10;
					state.ClampOffset();
					break handling;
				case LEFT:
					state.xOffset += 10;
					state.ClampOffset();
					break handling;
				case RIGHT:
					state.xOffset -= 10;
					state.ClampOffset();
					break handling;
				}
				switch (k) {
				case KEY_NUM1:
					state = state.ZoomOut();
					break handling;
				case KEY_NUM3:
					state = state.ZoomIn();
					break handling;
				case KEY_NUM7:
					BeginTextSearch();
					break handling;
				case KEY_NUM9:
					ShowGeo();
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

	protected void keyRepeated(int k) {
		// "home" button
		if (k == -12)
			return;

		touch = false;
		int ga = 0;
		try {
			ga = getGameAction(k);
		} catch (IllegalArgumentException e) { // j2l moment
		}
		if (mapFocused) {
			int val;
			if (repeatCount < 25) {
				val = 2 * repeatCount;
			} else {
				val = 50;
			}
			switch (ga) {
			case UP:
				state.yOffset += val;
				break;
			case DOWN:
				state.yOffset -= val;
				break;
			case LEFT:
				state.xOffset += val;
				break;
			case RIGHT:
				state.xOffset -= val;
				break;
			default:
				return;
			}
			state.ClampOffset();
			repeatCount++;
			requestRepaint();
		}
	}

	protected void keyReleased(int k) {
		repeatCount = 0;
		requestRepaint();
	}

	protected void pointerPressed(int x, int y, int n) {
		touch = true;
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

			if (y > getHeight() - overlays.overlaysH)
				break handling;

			if (overlays.OnGeopointTap(x, y))
				break handling;

			// tap at map
			if (MahoMapsApp.lastSearch == null) {
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
	}

	public void BeginTextSearch() {
		if (CheckApiAcsess()) {
			if (MahoMapsApp.lastSearch == null) {
				MahoMapsApp.BringSubScreen(searchBox);
			}
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == searchBox) {
			if (c == back) {
				MahoMapsApp.BringMap();
			} else {
				overlays.CloseOverlay(SelectOverlay.ID);
				Geopoint sa = GetSearchAnchor();
				MahoMapsApp.BringSubScreen(new SearchScreen(searchBox.getString(), sa));
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
	}

	protected void showNotify() {
		cachedGraphics = null;
		hidden = false;
		super.showNotify();
		requestRepaint();
	}
}

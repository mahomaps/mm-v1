package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import mahomaps.map.TileId;
import mahomaps.map.TilesProvider;

public class MapCanvas extends GameCanvas implements CommandListener {

	private TilesProvider tiles;
	private Command back = new Command("Назад", Command.BACK, 0);
	private Command routes = new Command("Маршрут", Command.ITEM, 1);
	private Command search = new Command("Поиск", Command.ITEM, 2);
	private Command settings = new Command("Настройки", Command.ITEM, 3);
	private Command about = new Command("О программе", Command.ITEM, 4);
	private Command moreapps = new Command("Другие программы", Command.ITEM, 5);

	String[] buttons = new String[] { "geo", "-", "+" };

	public MapCanvas(TilesProvider tiles) {
		super(false);
		this.tiles = tiles;
		addCommand(back);
		addCommand(routes);
		addCommand(search);
		addCommand(settings);
		addCommand(about);
		addCommand(moreapps);
		setCommandListener(this);
	}

	private void repaint(Graphics g) {
		final int w = getWidth();
		final int h = getHeight();
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, w, h);
		drawMap(g, w, h);
		drawOverlay(g, w, h);
		drawUi(g, w, h);
	}

	private void drawMap(Graphics g, int w, int h) {
		int y = 0;
		int yi=0;
		while(y<h) {
			int x = 0;
			int xi=0;
			while(x<w) {
				g.drawImage(tiles.getTile(new TileId(xi, yi, 0)), x, y, 0);
				x+=256;
				xi++;
			}
			y+=256;
			yi++;
		}
	}

	private void drawOverlay(Graphics g, int w, int h) {

	}

	private void drawUi(Graphics g, int w, int h) {
		int size = 50;
		int margin = 10;
		int y = h;
		for (int i = 0; i < 3; i++) {
			y -= size;
			y -= margin;
			g.setGrayScale(220);
			g.fillArc(w - size - margin, y, size, size, 0, 360);
			g.setColor(0);
			g.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
			g.drawString(buttons[i], w - margin - size / 2, y + size / 2 - g.getFont().getHeight() / 2,
					Graphics.HCENTER | Graphics.TOP);
		}
	}

	public void update() {
		repaint(getGraphics());
		flushGraphics();
	}

	public void commandAction(Command c, Displayable d) {

	}

}

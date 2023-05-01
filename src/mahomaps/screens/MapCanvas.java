package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import mahomaps.map.TilesProvider;

public class MapCanvas extends GameCanvas implements CommandListener {

	private TilesProvider tiles;
	private Command back = new Command("Назад", Command.BACK, 0);
	private Command routes = new Command("Маршрут", Command.ITEM, 1);
	private Command search = new Command("Поиск", Command.ITEM, 2);
	private Command settings = new Command("Настройки", Command.ITEM, 3);
	private Command about = new Command("О программе", Command.ITEM, 4);
	private Command moreapps = new Command("Другие программы", Command.ITEM, 5);

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

	}

	private void drawOverlay(Graphics g, int w, int h) {

	}

	private void drawUi(Graphics g, int w, int h) {
		g.setColor(0);
		g.drawString("ябаи-десуне", 0, 0, 0);
	}

	public void update() {
		repaint(getGraphics());
		flushGraphics();
	}

	public void commandAction(Command c, Displayable d) {

	}

}

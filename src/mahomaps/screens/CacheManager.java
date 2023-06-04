package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.TilesProvider;

public class CacheManager extends Form implements CommandListener, ItemCommandListener, Runnable {

	StringItem delAll = new StringItem(null, "Удалить все тайлы", Item.BUTTON);
	private Command back = new Command("Назад", Command.BACK, 0);
	private Command sel = new Command("Выбрать", Command.OK, 0);
	private TilesProvider tiles;

	public CacheManager(TilesProvider tiles) {
		super("Управление кэшем");
		this.tiles = tiles;
		addCommand(back);
		setCommandListener(this);
		append(new StringItem("Хранилище", getCacheType()));
		append(new StringItem("Тайлов хранится", "" + tiles.GetCachedTilesCount()));
		delAll.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		delAll.setDefaultCommand(sel);
		delAll.setItemCommandListener(this);
		append(delAll);
	}

	private static String getCacheType() {
		switch (Settings.cacheMode) {
		case Settings.CACHE_DISABLED:
			return "Не используется";
		case Settings.CACHE_FS:
			return "Файловая система";
		case Settings.CACHE_RMS:
			return "RMS";
		default:
			return null;
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back)
			MahoMapsApp.BringMenu();
	}

	public void commandAction(Command c, Item item) {
		if (c == sel) {
			if (item == delAll) {
				deleteAll();
				removeCommand(back);
				append(new Gauge("Очистка кэша", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
				(new Thread(this, "Cache clear")).start();
			}
		}
	}

	public void run() {
		tiles.ClearCache();
		MahoMapsApp.Exit();
	}
}

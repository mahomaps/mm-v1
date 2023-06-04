package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.TilesProvider;

public class CacheManager extends Form implements CommandListener {

	StringItem delAll = new StringItem(null, "Удалить все тайлы", Item.BUTTON);
	private Command back = new Command("Назад", Command.BACK, 0);

	public CacheManager(TilesProvider tiles) {
		super("Управление кэшем");
		addCommand(back);
		setCommandListener(this);
		append(new StringItem("Хранилище", getCacheType()));
		append(new StringItem("Тайлов хранится", "" + tiles.GetCachedTilesCount()));
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
}

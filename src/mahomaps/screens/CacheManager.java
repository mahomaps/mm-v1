/*
Copyright (c) 2023 Fyodor Ryzhov
*/
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

	StringItem delAll = new StringItem(null, MahoMapsApp.text[70], Item.BUTTON);
	private Command sel = new Command(MahoMapsApp.text[29], Command.OK, 0);
	private TilesProvider tiles;

	public CacheManager(TilesProvider tiles) {
		super(MahoMapsApp.text[69]);
		this.tiles = tiles;
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
		append(new StringItem(MahoMapsApp.text[71], getCacheType()));
		append(new StringItem(MahoMapsApp.text[72], "" + tiles.GetCachedTilesCount()));
		if (Settings.cacheMode == Settings.CACHE_FS)
			append(new StringItem(MahoMapsApp.text[71], tiles.GetLocalPath()));
		delAll.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		delAll.setDefaultCommand(sel);
		delAll.setItemCommandListener(this);
		append(delAll);
	}

	private static String getCacheType() {
		switch (Settings.cacheMode) {
		case Settings.CACHE_DISABLED:
			return MahoMapsApp.text[73];
		case Settings.CACHE_FS:
			return MahoMapsApp.text[53];
		case Settings.CACHE_RMS:
			return "RMS";
		default:
			return null;
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back)
			MahoMapsApp.BringMenu();
	}

	public void commandAction(Command c, Item item) {
		if (c == sel) {
			if (item == delAll) {
				deleteAll();
				removeCommand(MahoMapsApp.back);
				append(new Gauge(MahoMapsApp.text[74], false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
				append(new StringItem(null, MahoMapsApp.text[75]));
				(new Thread(this, "Cache clear")).start();
			}
		}
	}

	public void run() {
		tiles.ClearCache();
		MahoMapsApp.Exit();
	}
}

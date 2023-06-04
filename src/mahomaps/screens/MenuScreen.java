package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import mahomaps.MahoMapsApp;
import mahomaps.map.TilesProvider;

public class MenuScreen extends List implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);
	private TilesProvider tiles;

	public MenuScreen(TilesProvider tiles) {
		super("MahoMaps v1", Choice.IMPLICIT, new String[] { "Подключение к API", "Справка по клавиатуре", "Настройки",
				"Кэш", "О программе", "Другие программы", "Выход" }, null);
		this.tiles = tiles;
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == back) {
				MahoMapsApp.BringMap();
			} else if (c == SELECT_COMMAND) {
				int sel = getSelectedIndex();
				if (sel == 0) {
					MahoMapsApp.BringSubScreen(new APIReconnectForm());
				} else if (sel == 1) {
					MahoMapsApp.BringSubScreen(new KeyboardHelpScreen());
				} else if (sel == 2) {
					MahoMapsApp.BringSubScreen(new SettingsScreen());
				} else if (sel == 3) {
					MahoMapsApp.BringSubScreen(new CacheManager(tiles));
				} else if (sel == 4) {
					MahoMapsApp.BringSubScreen(new AboutScreen());
				} else if (sel == 5) {
					MahoMapsApp.BringSubScreen(new OtherAppsScreen());
				} else if (sel == 6) {
					MahoMapsApp.Exit();
				}
			}
		}
	}
}

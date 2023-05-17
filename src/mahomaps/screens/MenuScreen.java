package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class MenuScreen extends List implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command search = new Command("Поиск", Command.OK, 1);
	private TextBox searchBox = new TextBox("Поиск", "", 100, 0);

	public MenuScreen() {
		super("MahoMaps v1", Choice.IMPLICIT, new String[] { "Поиск", "Подключение к API", "Справка по клавиатуре", "Настройки",
				"О программе", "Другие программы", }, null);
		addCommand(back);
		setCommandListener(this);

		searchBox.addCommand(back);
		searchBox.addCommand(search);
		searchBox.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == back) {
				MahoMapsApp.BringMap();
			} else if (c == SELECT_COMMAND) {
				int sel = getSelectedIndex();
				if (sel == 0) {
					if (MahoMapsApp.lastSearch == null)
						MahoMapsApp.BringSubScreen(searchBox);
					else
						MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
				} else if (sel == 1) {
					MahoMapsApp.BringSubScreen(new APIReconnectForm());
				} else if (sel == 2) {
					MahoMapsApp.BringSubScreen(new KeyboardHelpScreen());
				} else if (sel == 3) {
					MahoMapsApp.BringSubScreen(new SettingsScreen());
				} else if (sel == 4) {
					MahoMapsApp.BringSubScreen(new AboutScreen());
				} else if (sel == 5) {
					MahoMapsApp.BringSubScreen(new OtherAppsScreen());
				}
			}
		} else if (d == searchBox) {
			if (c == back) {
				MahoMapsApp.BringMenu();
			} else {
				Geopoint geo = MahoMapsApp.GetCanvas().GetSearchAnchor();
				MahoMapsApp.BringSubScreen(new SearchScreen(searchBox.getString(), geo));
			}
		}
	}
}

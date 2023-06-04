package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import mahomaps.MahoMapsApp;

public class MenuScreen extends List implements CommandListener {

	public MenuScreen() {
		super("MahoMaps v1", Choice.IMPLICIT, new String[] { "Подключение к API", "Справка по клавиатуре", "Настройки",
				"О программе", "Другие программы", "Выход" }, null);
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == MahoMapsApp.back) {
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
					MahoMapsApp.BringSubScreen(new AboutScreen());
				} else if (sel == 4) {
					MahoMapsApp.BringSubScreen(new OtherAppsScreen());
				} else if (sel == 5) {
					MahoMapsApp.Exit();
				}
			}
		}
	}
}

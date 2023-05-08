package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import mahomaps.MahoMapsApp;

public class MenuScreen extends List implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);

	public MenuScreen() {
		super("MahoMaps", Choice.IMPLICIT, new String[] { "Поиск", "Маршрут", "Справка по клавиатуре", "Настройки",
				"О программе", "Другие программы", }, null);
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMap();
		} else if (c == SELECT_COMMAND) {
			int sel = getSelectedIndex();
			if (sel == 0) {
				// search
			} else if (sel == 1) {
				// route
			} else if (sel == 2) {
				// help
			} else if (sel == 3) {
				// sets
			} else if (sel == 4) {
				MahoMapsApp.BringSubScreen(new AboutScreen());
			} else if (sel == 5) {
				MahoMapsApp.BringSubScreen(new OtherAppsScreen());
			}
		}
	}
}

package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class SettingsScreen extends Form implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);

	public SettingsScreen() {
		super("Настройки");
		append(new StringItem("Типа настройки", "Тут ничего нет, но мы обязательно притащим"));
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
		}
	}
}

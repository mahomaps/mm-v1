package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class KeyboardHelpScreen extends Form implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);

	public KeyboardHelpScreen() {
		super("Справка");
		append(new StringItem("Навигация", "D-PAD"));
		append(new StringItem("Масштаб", "1/* - отдалить\n3/# - приблизить"));
		append(new StringItem("Поиск", "7"));
		append(new StringItem("Геопозиция", "9"));
		append(new StringItem("Меню приложения", "Левая софт-клавиша"));
		append(new StringItem("Меню карты", "Правая софт-клавиша"));
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
		}
	}
}

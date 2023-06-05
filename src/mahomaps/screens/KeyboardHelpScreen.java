package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class KeyboardHelpScreen extends Form implements CommandListener {

	public KeyboardHelpScreen() {
		super("Справка");
		append(new StringItem("Навигация", "D-PAD"));
		append(new StringItem("Масштаб", "1/* - приблизить\n3/# - отдалить"));
		append(new StringItem("Поиск", "7"));
		append(new StringItem("Геопозиция", "9"));
		append(new StringItem("Меню приложения", "Левая софт-клавиша"));
		append(new StringItem("Меню карты", "Правая софт-клавиша"));
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMenu();
		}
	}
}

package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class KeyboardHelpScreen extends Form implements CommandListener {

	public KeyboardHelpScreen() {
		super("Справка");
		add(new StringItem("Навигация", "D-PAD"));
		add(new StringItem("Масштаб", "1/* - приблизить\n3/# - отдалить"));
		add(new StringItem("Поиск", "7"));
		add(new StringItem("Геопозиция", "9"));
		add(new StringItem("Меню приложения", "Левая софт-клавиша"));
		add(new StringItem("Меню карты", "Правая софт-клавиша"));
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
	}
	
	private void add(StringItem item) {
		item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(item);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMenu();
		}
	}
}

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
		append(new StringItem("Клавиатура пока не поддерживается", "Прям совсем."));
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
		}
	}
}

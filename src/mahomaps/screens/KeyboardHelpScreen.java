/*
Copyright (c) 2023 Fyodor Ryzhov
*/
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
		super(MahoMapsApp.text[10]);
		add(new StringItem(MahoMapsApp.text[76], "D-PAD"));
		add(new StringItem(MahoMapsApp.text[77], "*, #"));
		add(new StringItem(MahoMapsApp.text[78], "1"));
		add(new StringItem(MahoMapsApp.text[152], "3"));
		add(new StringItem(MahoMapsApp.text[27], "7"));
		add(new StringItem(MahoMapsApp.text[148], "9"));
		add(new StringItem(MahoMapsApp.text[153], "0"));
		add(new StringItem(MahoMapsApp.text[79], MahoMapsApp.text[81]));
		add(new StringItem(MahoMapsApp.text[80], MahoMapsApp.text[82]));
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

/*
Copyright (c) 2023 Fyodor Ryzhov
Copyright (c) 2023 Petrprogs
*/
package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class UpdateScreen extends Form implements ItemCommandListener, CommandListener {

	private String url;

	public UpdateScreen(String text, String url) {
		super("MahoMaps v1");
		this.url = url;
		if (text == null)
			text = MahoMapsApp.text[26];

		append(text);
		if (url != null) {
			StringItem b = new StringItem(null, MahoMapsApp.text[168], Item.BUTTON);
			b.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
			b.addCommand(MahoMapsApp.openLink);
			b.setDefaultCommand(MahoMapsApp.openLink);
			b.setItemCommandListener(this);
			append(b);
		}

		addCommand(MahoMapsApp.back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMap();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == MahoMapsApp.openLink) {
			MahoMapsApp.open(url);
		}
	}
}

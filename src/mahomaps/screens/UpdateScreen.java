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

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command openLink = new Command("Открыть", Command.ITEM, 1);

	public UpdateScreen(String text, String url) {
		super("MahoMaps v1");
		this.url = url;
		if (text == null)
			text = "Доступна новая версия.";

		append(text);
		if (url != null) {
			StringItem b = new StringItem(null, "Открыть", Item.BUTTON);
			b.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
			b.addCommand(openLink);
			b.setDefaultCommand(openLink);
			b.setItemCommandListener(this);
			append(b);
		}

		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMap();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == openLink) {
			MahoMapsApp.open(url);
		}
	}
}

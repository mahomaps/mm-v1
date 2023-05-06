package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class AboutScreen extends Form implements CommandListener, ItemCommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command openLink = new Command("Открыть", Command.ITEM, 1);

	StringItem website = new StringItem("Сайт", "nnp.nnchan.ru", Item.HYPERLINK);
	StringItem chat = new StringItem("Чат", "t.me/nnmidletschat", Item.HYPERLINK);

	public AboutScreen() {
		super("О программе");
		setCommandListener(this);
		addCommand(back);

		append(new StringItem("MahoMaps", "J2ME клиент растровых Яндекс.Карт.\nВерсия 1." + MahoMapsApp.version));
		try {
			append(Image.createImage("/icon.png"));
		} catch (IOException e) {
		}
		append(new StringItem("Программирование", "Shinovon\nFeodor0090 (aka sym_ansel)"));
		append(new StringItem("Дизайн", "MuseCat"));
		append(new StringItem("Благодарности", "vipaoL\nGingerFox87\nCygames\nLantis"));
		website.addCommand(openLink);
		website.setDefaultCommand(openLink);
		website.setItemCommandListener(this);
		append(website);
		chat.addCommand(openLink);
		chat.setDefaultCommand(openLink);
		chat.setItemCommandListener(this);
		append(chat);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMap();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == openLink) {
			if (item == website) {
				MahoMapsApp.open("http://nnp.nnchan.ru");
			} else if (item == chat) {
				MahoMapsApp.open("http://mp.nnchan.ru/chat.php?c=nnmidletschat");
			}
		}
	}

}

package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class AboutScreen extends Form implements CommandListener, ItemCommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command openLink = new Command("Открыть", Command.ITEM, 1);

	StringItem website = new StringItem("Сайт", "nnp.nnchan.ru", Item.HYPERLINK);
	StringItem chat = new StringItem("Чат", "t.me/nnmidletschat", Item.HYPERLINK);
	StringItem gh = new StringItem("GitHub", "github.com/mahomaps", Item.HYPERLINK);

	public AboutScreen() {
		super("О программе");
		setCommandListener(this);
		addCommand(back);
		try {
			ImageItem i = new ImageItem(null, Image.createImage("/icon.png"),
					Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT, "logo");
			append(i);
		} catch (IOException e) {
		}
		StringItem s;
		append(s = new StringItem("MahoMaps", "J2ME клиент растровых Яндекс.Карт.\nВерсия 1." + MahoMapsApp.version));
		if(MahoMapsApp.platform != null && MahoMapsApp.platform.indexOf("S60") != -1 && MahoMapsApp.platform.indexOf("platform_version=3.2") == -1) {
			// фокус на начало экрана
			try {
				MahoMapsApp.display.setCurrentItem(s);
			} catch (Throwable e) {
			}
		}
		append(new StringItem("Предложил", "GingerFox87"));
		append(new StringItem("Тимлид", "Feodor0090 (aka sym_ansel)"));
		append(new StringItem("Поддержать нас рублём", "5536 9141 0062 0677"));
		append(new StringItem("Программирование", "Feodor0090 (aka sym_ansel)\nShinovon"));
		append(new StringItem("Дизайн", "MuseCat"));
		append(new StringItem("Прокси", "Shinovon\nrehdzi"));
		append(new StringItem("CI/CD", "vipaoL"));
		append(new StringItem("Тестеры", "MuseCat\nGingerFox87\nДмитрий Михно\nvipaoL"));
		append(new StringItem("Группа поддержки", "stacorp"));
		append(new StringItem("Поиграйте в игры от", "Cygames"));
		append(new StringItem("Писалось под музыку от", "Lantis"));
		website.addCommand(openLink);
		website.setDefaultCommand(openLink);
		website.setItemCommandListener(this);
		append(website);
		chat.addCommand(openLink);
		chat.setDefaultCommand(openLink);
		chat.setItemCommandListener(this);
		append(chat);
		gh.addCommand(openLink);
		gh.setDefaultCommand(openLink);
		gh.setItemCommandListener(this);
		append(gh);
		append(new StringItem("Послесловие", "Powered by butthurt from nnchat\n292 labs (tm)"));
		append(new StringItem("Реклама", "Гитхаб Анселя:\ngithub.com/Feodor0090\n"
				+ "Канал Анселя:\nt.me/sym_ansel_blog\n"
				+ "Борда rehdzi:\nnnchan.ru\n"
				+ "Канал Димы:\nt.me/blogprostodimonich\n"
				+ "Канал Лиса:\nt.me/GingerFox87_blog\n"
				+ "Игра Выполя:\nt.me/mobap_game\n"));
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
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

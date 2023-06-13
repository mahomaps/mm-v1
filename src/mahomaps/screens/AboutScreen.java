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

	StringItem website = new StringItem(MahoMapsApp.text[33], "nnp.nnchan.ru", Item.HYPERLINK);
	StringItem chat = new StringItem(MahoMapsApp.text[34], "t.me/nnmidletschat", Item.HYPERLINK);
	StringItem gh = new StringItem("GitHub", "github.com/mahomaps", Item.HYPERLINK);

	int i1, i2, i3, i4;

	public AboutScreen() {
		super(MahoMapsApp.text[12]);
		SetF();
		setCommandListener(this);
		addCommand(MahoMapsApp.back);
		try {
			Image img = Image.createImage("/splash.png");
			ImageItem i = new ImageItem(null, img, Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER, "logo");
			append(i);
		} catch (IOException e) {
		}
		StringItem s = new StringItem("MahoMaps",
				MahoMapsApp.text[35] + "\n" + MahoMapsApp.text[36] + " 1." + MahoMapsApp.version);
		add(s);
		if (MahoMapsApp.platform != null && MahoMapsApp.platform.indexOf("S60") != -1
				&& MahoMapsApp.platform.indexOf("platform_version=3.2") == -1) {
			// фокус на начало экрана
			try {
				MahoMapsApp.display.setCurrentItem(s);
			} catch (Throwable e) {
			}
		}
		SetI();
		add(new StringItem("Предложил", "GingerFox87"));
		add(new StringItem("Тимлид", "Feodor0090 (aka sym_ansel)"));
		add(new StringItem("Поддержать нас рублём",
				s(i1) + " " + s(i2) + " " + s(i3) + " " + s(i4)));
		add(new StringItem("Программирование", "Feodor0090 (aka sym_ansel)\nShinovon"));
		add(new StringItem("Дизайн", "MuseCat"));
		add(new StringItem("Прокси", "Shinovon\nrehdzi"));
		add(new StringItem("CI/CD", "vipaoL"));
		add(new StringItem("Тестеры", "MuseCat\nGingerFox87\nДмитрий Михно\nvipaoL"));
		add(new StringItem("Группа поддержки", "stacorp"));
		add(new StringItem("Поиграйте в игры от", "Cygames"));
		add(new StringItem("Писалось под музыку от", "Lantis"));
		website.setDefaultCommand(MahoMapsApp.openLink);
		website.setItemCommandListener(this);
		add(website);
		chat.setDefaultCommand(MahoMapsApp.openLink);
		chat.setItemCommandListener(this);
		add(chat);
		gh.setDefaultCommand(MahoMapsApp.openLink);
		gh.setItemCommandListener(this);
		add(gh);
		try {
			ImageItem i = new ImageItem(null, Image.createImage("/stick.png"),
					Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER, "торшер");
			append(i);
		} catch (IOException e) {
		}
		add(new StringItem(MahoMapsApp.text[67], "Powered by butthurt from nnchat\n292 labs (tm)"));
		add(new StringItem(MahoMapsApp.text[68],
				"Гитхаб Анселя:\ngithub.com/Feodor0090\n" + "Канал Анселя:\nt.me/sym_ansel_blog\n"
						+ "Борда rehdzi:\nnnchan.ru\n" + "Канал Димы:\nt.me/blogprostodimonich\n"
						+ "Канал Лиса:\nt.me/GingerFox87_blog\n" + "Игра Выполя:\nt.me/mobap_game\n"));
	}

	private String s(int i) {
		String s = String.valueOf(i);
		while(s.length() < 4) s = "0" + s;
		return s;
	}

	private void add(StringItem item) {
		item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
		append(item);
	}

	private void SetI() {
		i1 = 5536;
		i2 = 9141;
		i3 = 0062;
		i4 = 0677;
	}

	private void SetF() {
		i1 = 1;
		i2 = 2;
		i3 = 3;
		i4 = 4;
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMenu();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == MahoMapsApp.openLink) {
			if (item == website) {
				MahoMapsApp.open("http://nnp.nnchan.ru");
			} else if (item == chat) {
				MahoMapsApp.open("http://mp.nnchan.ru/chat.php?c=nnmidletschat");
			} else if (item == gh) {
				MahoMapsApp.open("https://github.com/mahomaps");
			}
		}
	}

}

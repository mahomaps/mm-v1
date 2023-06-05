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
			Image img = Image.createImage(MahoMapsApp.GetCanvas().getWidth() > 300 ? "/icon256.png" : "/icon.png");
			ImageItem i = new ImageItem(null, img, Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER, "logo");
			append(i);
		} catch (IOException e) {
		}
		StringItem s = new StringItem("MahoMaps",
				MahoMapsApp.text[35] + "\n" + MahoMapsApp.text[36] + " 1." + MahoMapsApp.version);
		s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(s);
		if (MahoMapsApp.platform != null && MahoMapsApp.platform.indexOf("S60") != -1
				&& MahoMapsApp.platform.indexOf("platform_version=3.2") == -1) {
			// фокус на начало экрана
			try {
				MahoMapsApp.display.setCurrentItem(s);
			} catch (Throwable e) {
			}
		}
		SetI();
		append(new StringItem("Предложил", "GingerFox87"));
		append(new StringItem("Тимлид", "Feodor0090 (aka sym_ansel)"));
		append(new StringItem("Поддержать нас рублём",
				String.valueOf(i1) + " " + String.valueOf(i2) + " " + String.valueOf(i3) + " " + String.valueOf(i4)));
		append(new StringItem("Программирование", "Feodor0090 (aka sym_ansel)\nShinovon"));
		append(new StringItem("Дизайн", "MuseCat"));
		append(new StringItem("Прокси", "Shinovon\nrehdzi"));
		append(new StringItem("CI/CD", "vipaoL"));
		append(new StringItem("Тестеры", "MuseCat\nGingerFox87\nДмитрий Михно\nvipaoL"));
		append(new StringItem("Группа поддержки", "stacorp"));
		append(new StringItem("Поиграйте в игры от", "Cygames"));
		append(new StringItem("Писалось под музыку от", "Lantis"));
		website.setDefaultCommand(MahoMapsApp.openLink);
		website.setItemCommandListener(this);
		append(website);
		chat.setDefaultCommand(MahoMapsApp.openLink);
		chat.setItemCommandListener(this);
		append(chat);
		gh.setDefaultCommand(MahoMapsApp.openLink);
		gh.setItemCommandListener(this);
		append(gh);
		try {
			ImageItem i = new ImageItem(null, Image.createImage("/stick.png"),
					Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER, "торшер");
			append(i);
		} catch (IOException e) {
		}
		s = new StringItem("Послесловие", "Powered by butthurt from nnchat\n292 labs (tm)");
		s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		append(s);
		append(new StringItem("Реклама",
				"Гитхаб Анселя:\ngithub.com/Feodor0090\n" + "Канал Анселя:\nt.me/sym_ansel_blog\n"
						+ "Борда rehdzi:\nnnchan.ru\n" + "Канал Димы:\nt.me/blogprostodimonich\n"
						+ "Канал Лиса:\nt.me/GingerFox87_blog\n" + "Игра Выполя:\nt.me/mobap_game\n"));
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

package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class OtherAppsScreen extends Form implements CommandListener, ItemCommandListener {

	StringItem vk = new StringItem("VK", "VK4ME (curoviyxru)", Item.HYPERLINK);
	StringItem tg = new StringItem("Telegram", "MPGram Web (shinovon)", Item.HYPERLINK);
	StringItem jt = new StringItem("YouTube", "JTube (shinovon)", Item.HYPERLINK);
	StringItem bt = new StringItem("Translator", "Bing Translate (shinovon)", Item.HYPERLINK);
	StringItem nm = new StringItem("osu! beatmaps player", "nmania (Feodor0090)", Item.HYPERLINK);
	StringItem kem = new StringItem("J2ME emulator for Windows", "KEmulator nnmod (shinovon)", Item.HYPERLINK);
	StringItem j2l = new StringItem("J2ME emulator for Android", "J2MEL (nikita36078)", Item.HYPERLINK);

	public OtherAppsScreen() {
		super("Другие программы");
		setCommandListener(this);
		addCommand(MahoMapsApp.back);

		append(new StringItem(null,
				"Мы собрали для вас некоторые другие наши приложения для доступа к актуальным сервисам."));
		appendLink(vk);
		appendLink(tg);
		appendLink(jt);
		appendLink(bt);
		appendLink(nm);
		appendLink(kem);
		appendLink(j2l);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMenu();
		}
	}

	private void appendLink(StringItem item) {
		item.setDefaultCommand(MahoMapsApp.openLink);
		item.setItemCommandListener(this);
		append(item);
	}

	public void commandAction(Command c, Item item) {
		if (c == MahoMapsApp.openLink) {
			if (item == vk) {
				MahoMapsApp.open("http://v4.crx.moe/");
			} else if (item == tg) {
				MahoMapsApp.open("http://mp.nnchan.ru/");
			} else if (item == jt) {
				MahoMapsApp.open("http://nnp.nnchan.ru/jtube/");
			} else if (item == bt) {
				MahoMapsApp.open("http://nnp.nnchan.ru/bingt/");
			} else if (item == nm) {
				MahoMapsApp.open("https://github.com/Feodor0090/nmania");
			} else if (item == kem) {
				MahoMapsApp.open("http://nnp.nnchan.ru/kem/");
			} else if (item == j2l) {
				MahoMapsApp.open("https://github.com/nikita36078/J2ME-Loader");
			}
		}
	}

}

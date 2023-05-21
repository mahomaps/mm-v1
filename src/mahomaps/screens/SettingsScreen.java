package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;

public class SettingsScreen extends Form implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);

	private ChoiceGroup focusZoom = new ChoiceGroup("Масштаб при просмотре точек", Choice.POPUP,
			new String[] { "15", "16", "17", "18" }, null);
	private ChoiceGroup geoLook;
	private ChoiceGroup geoStatus = new ChoiceGroup("Статус гео", Choice.EXCLUSIVE,
			new String[] { "Скрывать", "Показывать" }, null);
	private ChoiceGroup tileInfo = new ChoiceGroup("Отладка тайлов", Choice.EXCLUSIVE,
			new String[] { "Выключено", "Включено" }, null);
	private ChoiceGroup cache = new ChoiceGroup("Сохранять тайлы", Choice.EXCLUSIVE,
			new String[] { "Запрещено", "В файловую" }, null);
	private ChoiceGroup download = new ChoiceGroup("Скачивать тайлы", Choice.EXCLUSIVE,
			new String[] { "Запрещено", "Схема, светлая палитра", }, null);

	public SettingsScreen() {
		super("Настройки");
		addCommand(back);
		setCommandListener(this);
		Image[] imgs = null;
		try {
			int size = 20;
			Image sheet = Image.createImage("/geo" + size + ".png");
			Image i1 = Image.createImage(size, size);
			i1.getGraphics().drawImage(sheet, 0, 0, 0);
			Image i2 = Image.createImage(size, size);
			i2.getGraphics().drawImage(sheet, 0, -size, 0);
			Image i3 = Image.createImage(size, size);
			i3.getGraphics().drawImage(sheet, 0, -(size * 2), 0);
			imgs = new Image[] { i1, i2, i3 };
		} catch (IOException e) {
		}
		geoLook = new ChoiceGroup("Вид метки геопозиции", Choice.EXCLUSIVE, new String[] { "Капля", "\"Я\"", "\"Ы\"" },
				imgs);
		if (Settings.focusZoom < 15)
			Settings.focusZoom = 15;
		if (Settings.focusZoom > 18)
			Settings.focusZoom = 18;
		focusZoom.setSelectedIndex(Settings.focusZoom - 15, true);
		geoLook.setSelectedIndex(Settings.geoLook, true);
		geoStatus.setSelectedIndex(Settings.showGeo ? 1 : 0, true);
		tileInfo.setSelectedIndex(Settings.drawTileInfo ? 1 : 0, true);
		cache.setSelectedIndex(Settings.cacheMode, true);
		download.setSelectedIndex(Settings.allowDownload ? 1 : 0, true);

		append(focusZoom);
		append(geoLook);
		append(geoStatus);
		append(tileInfo);
		// append(cache);
		append(download);
	}

	private void Apply() {
		Settings.focusZoom = focusZoom.getSelectedIndex() + 15;
		Settings.geoLook = geoLook.getSelectedIndex();
		Settings.showGeo = geoStatus.getSelectedIndex() == 1;
		Settings.drawTileInfo = tileInfo.getSelectedIndex() == 1;
		Settings.cacheMode = cache.getSelectedIndex();
		Settings.allowDownload = download.getSelectedIndex() == 1;
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			Apply();
			Settings.Save();
			MahoMapsApp.BringMenu();
		}
	}
}

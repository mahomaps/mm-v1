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
	private ChoiceGroup geoStatus = new ChoiceGroup("Показывать геопозицию", Choice.POPUP,
			new String[] { "Только метку на карте", "Статус", "Статус и координаты" }, null);
	private ChoiceGroup tileInfo = new ChoiceGroup("Отладочная информация", Choice.POPUP,
			new String[] { "Выключено", "Включено" }, null);
	private ChoiceGroup cache = new ChoiceGroup("Сохранять тайлы", Choice.POPUP,
			new String[] { "Запрещено", "В файловую", "RMS" }, null);
	private ChoiceGroup download = new ChoiceGroup("Скачивать тайлы", Choice.POPUP,
			new String[] { "Запрещено", "Схема, светлая палитра", }, null);
	private ChoiceGroup proxyTiles = new ChoiceGroup("Проксирование", Choice.POPUP,
			new String[] { "Отключено", "nnchan.ru", }, null);

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
			Image i4 = Image.createImage(size, size);
			i4.getGraphics().drawImage(sheet, 0, -(size * 3), 0);
			imgs = new Image[] { i1, i2, i3, i4 };
		} catch (IOException e) {
		}
		geoLook = new ChoiceGroup("Вид метки геопозиции", Choice.EXCLUSIVE,
				new String[] { "Капля", "\"Я\"", "\"Ы\"", "\"Ъ\"" }, imgs);
		if (Settings.focusZoom < 15)
			Settings.focusZoom = 15;
		if (Settings.focusZoom > 18)
			Settings.focusZoom = 18;
		focusZoom.setSelectedIndex(Settings.focusZoom - 15, true);
		geoLook.setSelectedIndex(Settings.geoLook, true);
		geoStatus.setSelectedIndex(Settings.showGeo, true);
		tileInfo.setSelectedIndex(Settings.drawDebugInfo ? 1 : 0, true);
		cache.setSelectedIndex(Settings.cacheMode, true);
		download.setSelectedIndex(Settings.allowDownload ? 1 : 0, true);
		proxyTiles.setSelectedIndex(Settings.proxyTiles ? 1 : 0, true);
		// апи отслеживается отдельно, однако предполагается что оно включено вместе с
		// тайлами.

		append(focusZoom);
		append(geoLook);
		append(geoStatus);
		append(tileInfo);
		append(cache);
		append(download);
		append(proxyTiles);
	}

	private void Apply() {
		Settings.focusZoom = focusZoom.getSelectedIndex() + 15;
		Settings.geoLook = geoLook.getSelectedIndex();
		Settings.showGeo = geoStatus.getSelectedIndex();
		Settings.drawDebugInfo = tileInfo.getSelectedIndex() == 1;
		Settings.cacheMode = cache.getSelectedIndex();
		Settings.allowDownload = download.getSelectedIndex() == 1;
		Settings.proxyTiles = proxyTiles.getSelectedIndex() == 1;
		Settings.proxyApi = proxyTiles.getSelectedIndex() == 1;
		if (Settings.allowDownload) {
			MahoMapsApp.tiles.ForceMissingDownload();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			Apply();
			if (!MahoMapsApp.TryInitFSCache(false))
				return;
			Settings.Save();
			MahoMapsApp.BringMenu();
		}
	}
}

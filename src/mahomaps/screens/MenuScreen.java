package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import mahomaps.MahoMapsApp;
import mahomaps.map.TilesProvider;

public class MenuScreen extends List implements CommandListener {

	private final TilesProvider tiles;

	public MenuScreen(TilesProvider tiles) {
		super("MahoMaps v1", Choice.IMPLICIT,
				new String[] { MahoMapsApp.text[148], MahoMapsApp.text[153], MahoMapsApp.text[9], MahoMapsApp.text[10],
						MahoMapsApp.text[11], MahoMapsApp.text[69], MahoMapsApp.text[12],
						MahoMapsApp.text[0] },
				null);
		this.tiles = tiles;
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == MahoMapsApp.back) {
				MahoMapsApp.BringMap();
			} else if (c == SELECT_COMMAND) {
				int sel = getSelectedIndex();
				if (sel == 0) {
					MahoMapsApp.BringSubScreen(new BookmarksScreen());
				} else if (sel == 1) {
					MahoMapsApp.BringSubScreen(new MapLayerSelectionScreen());
				} else if (sel == 2) {
					MahoMapsApp.BringSubScreen(new APIReconnectForm());
				} else if (sel == 3) {
					MahoMapsApp.BringSubScreen(new KeyboardHelpScreen());
				} else if (sel == 4) {
					MahoMapsApp.BringSubScreen(new SettingsScreen());
				} else if (sel == 5) {
					MahoMapsApp.BringSubScreen(new CacheManager(tiles));
				} else if (sel == 6) {
					MahoMapsApp.BringSubScreen(new AboutScreen());
				} else if (sel == 7) {
					MahoMapsApp.Exit();
				}
			}
		}
	}
}

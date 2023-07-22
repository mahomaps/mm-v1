package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.TilesProvider;

public class MapLayerSelectionScreen extends List implements CommandListener {

	public MapLayerSelectionScreen() {
		super(MahoMapsApp.text[153], IMPLICIT, TilesProvider.GetLayerNames(), null);
		setSelectedIndex(Settings.map, true);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		Settings.map = getSelectedIndex();
		Settings.Save();
		MahoMapsApp.BringMap();
	}

}

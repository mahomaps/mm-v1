/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.screens.SettingsScreen;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class TileDownloadForbiddenOverlay extends MapOverlay implements IButtonHandler {
	public TileDownloadForbiddenOverlay() {
		content = new FillFlowContainer(new UIElement[] { new SimpleText(MahoMapsApp.text[42]),
				new SimpleText(MahoMapsApp.text[43]), new ColumnsContainer(new UIElement[] {
						new Button(MahoMapsApp.text[41], 1, this), new Button(MahoMapsApp.text[38], 0, this) }) });
	}

	public String GetId() {
		return "no_network";
	}

	public Vector GetPoints() {
		return EMPTY_VECTOR;
	}

	public boolean OnPointTap(Geopoint p) {
		return false;
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case 0:
			Close();
			break;
		case 1:
			MahoMapsApp.BringSubScreen(new SettingsScreen());
			Close();
			break;
		}

	}
}

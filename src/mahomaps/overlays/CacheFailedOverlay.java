/*
Copyright (c) 2023 Petrprogs
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class CacheFailedOverlay extends MapOverlay implements IButtonHandler {

	public CacheFailedOverlay() {
		content = new FillFlowContainer(
				new UIElement[] { new SimpleText(MahoMapsApp.text[124]), new SimpleText(MahoMapsApp.text[125]),
						new ColumnsContainer(new UIElement[] { new Button(MahoMapsApp.text[38], 0, this) }) });
	}

	public String GetId() {
		return "cache_fail";
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
		}

	}

}

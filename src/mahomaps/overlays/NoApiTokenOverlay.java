package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.screens.APIReconnectForm;
import mahomaps.ui.Button;
import mahomaps.ui.CloseButton;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class NoApiTokenOverlay extends MapOverlay implements IButtonHandler {

	public NoApiTokenOverlay() {
		content = new FillFlowContainer(
				new UIElement[] { new SimpleText(MahoMapsApp.text[39]), new SimpleText(MahoMapsApp.text[40]),
						new Button(MahoMapsApp.text[37], 1, this), new CloseButton(this) });
	}

	public String GetId() {
		return "no_token";
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
			MahoMapsApp.BringSubScreen(new APIReconnectForm());
			Close();
			break;
		}

	}

}

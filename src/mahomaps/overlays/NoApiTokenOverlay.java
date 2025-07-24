package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.screens.APIReconnectForm;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class NoApiTokenOverlay extends MapOverlay implements IButtonHandler {

	public NoApiTokenOverlay() {
		content = new FillFlowContainer(new UIElement[] { new SimpleText(MahoMapsApp.text[39]),
				new SimpleText(MahoMapsApp.text[40]), new ColumnsContainer(new UIElement[] {
						new Button(MahoMapsApp.text[37], 1, this), new Button(MahoMapsApp.text[38], 0, this) }) });
	}

	public String GetId() {
		return "no_token";
	}

	public Vector GetPoints() {
		return EMPTY_VECTOR;
	}

	public int GetName() {
		return 39;
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

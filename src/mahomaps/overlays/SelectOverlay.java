package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.screens.SearchScreen;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class SelectOverlay extends MapOverlay implements IButtonHandler {

	public static final String ID = "selection";

	private final Geopoint selection;

	private final Vector v = new Vector(1);

	public SelectOverlay(final Geopoint p) {
		selection = new Geopoint(p.lat, p.lon);
		selection.type = Geopoint.POI_SELECT;
		selection.color = Geopoint.COLOR_RED;
		v.addElement(selection);

		content = new FillFlowContainer(
				new UIElement[] { new SimpleText(p.toString(), 0), new Button("Что здесь?", 1, this),
						new ColumnsContainer(
								new UIElement[] { new Button("Точка А", 2, this), new Button("Точка Б", 3, this) }),
						new Button("Закрыть", 0, this) });
	}

	public String GetId() {
		return ID;
	}

	public Vector GetPoints() {
		return v;
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
			if (MahoMapsApp.GetCanvas().CheckApiAcsess()) {
				Close();
				MahoMapsApp.BringSubScreen(new SearchScreen(selection.toString(), selection));
			}
			break;
		case 2:
			break;
		case 3:
			break;
		}
	}
}

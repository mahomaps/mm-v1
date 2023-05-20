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

public class RouteBuildOverlay extends MapOverlay implements IButtonHandler {

	private static final String ID = "route";
	private final Vector v = new Vector();
	private Geopoint a, b;

	public RouteBuildOverlay() {
		Update();
	}

	private void Update() {
		v.removeAllElements();
		if (a != null && b != null) {
			v.addElement(a);
			v.addElement(b);
			content = new FillFlowContainer(new UIElement[] { new ColumnsContainer(new UIElement[] {
					new Button("Авто", 1, this), new Button("Пешком", 2, this), new Button("Общ. тр.", 3, this)

					}), new Button("Отмена", 0, this) });
		} else if (a == null && b == null) {
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Выберите точки А и Б на карте.", 0),
					new Button("Отмена", 0, this) });
		} else {
			String s;
			if (b == null) {
				s = "Точка А: " + a.toString();
				v.addElement(a);
			} else {
				s = "Точка Б: " + b.toString();
				v.addElement(b);
			}
			content = new FillFlowContainer(new UIElement[] { new SimpleText(s, 0),
					new SimpleText("Выберите вторую точку.", 0), new Button("Отмена", 0, this) });
		}
	}

	public void SetA(Geopoint p) {
		a = new Geopoint(p.lat, p.lon);
		a.type = Geopoint.ROUTE_A;
		Update();
	}

	public void SetB(Geopoint p) {
		b = new Geopoint(p.lat, p.lon);
		b.type = Geopoint.ROUTE_B;
		Update();
	}

	public static RouteBuildOverlay Get() {
		MapOverlay mo = MahoMapsApp.GetCanvas().GetOverlay(ID);
		if (mo != null) {
			if (mo instanceof RouteBuildOverlay) {
				return (RouteBuildOverlay) mo;
			}
		}
		RouteBuildOverlay o = new RouteBuildOverlay();
		MahoMapsApp.GetCanvas().PushOverlay(o);
		return o;
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
		if (uid == 0) {
			Close();
			return;
		}
		if (a == null || b == null)
			return;
		switch (uid) {
		case 1:
			break;

		}

	}

}

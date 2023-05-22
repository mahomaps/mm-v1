package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.screens.MapCanvas;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class RouteBuildOverlay extends MapOverlay implements IButtonHandler {

	public static final String ID = "route";
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
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Выберите точки А и Б на карте."),
					new Button("Отмена", 0, this) });
		} else {
			String s;
			Button bt;
			if (b == null) {
				s = "Точка А: " + a.toString();
				bt = new Button("До моей геолокации", 10, this);
				v.addElement(a);
			} else {
				s = "Точка Б: " + b.toString();
				bt = new Button("От моей геолокации", 11, this);
				v.addElement(b);
			}
			content = new FillFlowContainer(new UIElement[] { new SimpleText(s),
					new SimpleText("Выберите вторую точку."), bt, new Button("Отмена", 0, this) });
		}
	}

	public void SetA(Geopoint p) {
		a = new Geopoint(p.lat, p.lon);
		a.type = Geopoint.ROUTE_A;
		Update();
		InvalidateSize();
	}

	public void SetB(Geopoint p) {
		b = new Geopoint(p.lat, p.lon);
		b.type = Geopoint.ROUTE_B;
		Update();
		InvalidateSize();
	}

	public static RouteBuildOverlay Get() {
		MapOverlay mo = MahoMapsApp.Overlays().GetOverlay(ID);
		if (mo != null) {
			if (mo instanceof RouteBuildOverlay) {
				return (RouteBuildOverlay) mo;
			}
		}
		RouteBuildOverlay o = new RouteBuildOverlay();
		MahoMapsApp.Overlays().PushOverlay(o);
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
		MapCanvas m = MahoMapsApp.GetCanvas();
		if (uid == 0) {
			Close();
			return;
		}
		if (uid == 10) {
			if (m.geo.DrawPoint()) {
				SetB(m.geolocation);
			}
			return;
		}
		if (uid == 11) {
			if (m.geo.DrawPoint()) {
				SetA(m.geolocation);
			}
			return;
		}
		if (a == null || b == null)
			return;
		if (!MahoMapsApp.GetCanvas().CheckApiAcsess())
			return;
		int method = 0;
		switch (uid) {
		case 1:
			method = YmapsApi.ROUTE_AUTO;
			break;
		case 2:
			method = YmapsApi.ROUTE_BYFOOT;
			break;
		case 3:
			method = YmapsApi.ROUTE_TRANSPORT;
			break;
		}

		MahoMapsApp.Overlays().PushOverlay(new RouteOverlay(a, b, method));
	}

}

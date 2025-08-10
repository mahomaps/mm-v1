package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
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
			content = new FillFlowContainer(new UIElement[]{
					new ColumnsContainer(new UIElement[]{
							new Button(MahoMapsApp.text[126], 1, this),
							new Button(MahoMapsApp.text[127], 2, this),
							new Button(MahoMapsApp.text[128], 3, this)
					}),
					new Button(MahoMapsApp.text[118], 0, this)
			});
		} else if (a == null && b == null) {
			content = new FillFlowContainer(new UIElement[]{
					new SimpleText(MahoMapsApp.text[129]),
					new Button(MahoMapsApp.text[118], 0, this)
			});
		} else {
			String s;
			Button bt;
			if (b == null) {
				s = MahoMapsApp.text[131];
				bt = new Button(MahoMapsApp.text[133], 10, this);
				v.addElement(a);
			} else {
				s = MahoMapsApp.text[130];
				bt = new Button(MahoMapsApp.text[132], 11, this);
				v.addElement(b);
			}
			content = new FillFlowContainer(new UIElement[]{
					new SimpleText(s),
					new ColumnsContainer(new UIElement[]{
							bt,
							new Button(MahoMapsApp.text[118], 0, this)
					})
			});
		}
	}

	public int GetName() {
		if (a != null && b != null) {
			return 116;
		} else if (a == null && b == null) {
			return 129;
		} else {
			// one point selected
			if (b == null) {
				return 131;
			} else {
				return 130;
			}
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

	private static void NotifyNoGeo() {
		Alert a = new Alert(MahoMapsApp.text[116], MahoMapsApp.text[117], null,
				AlertType.WARNING);
		a.setTimeout(4000);
		MahoMapsApp.BringSubScreen(a, MahoMapsApp.GetCanvas());
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
			if (m.geo != null && m.geo.DrawPoint()) {
				SetB(m.geolocation);
			} else {
				NotifyNoGeo();
			}
			return;
		}
		if (uid == 11) {
			if (m.geo != null && m.geo.DrawPoint()) {
				SetA(m.geolocation);
			} else {
				NotifyNoGeo();
			}
			return;
		}
		if (a == null || b == null)
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

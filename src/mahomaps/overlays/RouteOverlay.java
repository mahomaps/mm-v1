package mahomaps.overlays;

import java.util.Vector;

import mahomaps.MahoMapsApp;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.map.Line;
import mahomaps.map.Route;
import mahomaps.ui.Button;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class RouteOverlay extends MapOverlay implements Runnable, IButtonHandler {

	final Vector v = new Vector();
	private Geopoint a;
	private Geopoint b;
	private final int method;
	Route route;

	public RouteOverlay(Geopoint a, Geopoint b, int method) {
		this.a = a;
		this.b = b;
		this.method = method;
		v.addElement(a);
		v.addElement(b);
		content = new FillFlowContainer(new UIElement[] { new SimpleText("Загружаем маршрут...") });
		Thread th = new Thread(this, "Route api request");
		th.start();
	}

	public String GetId() {
		return RouteBuildOverlay.ID;
	}

	public Vector GetPoints() {
		return v;
	}

	public boolean OnPointTap(Geopoint p) {
		return false;
	}

	public void run() {
		try {
			route = new Route(MahoMapsApp.api.Route(a, b, method));
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Маршрут " + Type(method)),
					new SimpleText("Расстояние: " + route.distance), new SimpleText("Время: " + route.time),
					new Button("Закрыть", 0, this) });
			InvalidateSize();
			MahoMapsApp.GetCanvas().line = new Line(a, route.points);
		} catch (Exception e) {
			e.printStackTrace();
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Не удалось построить маршрут."),
					new Button("Закрыть", 0, this) });
			InvalidateSize();
		}
	}

	public void OnButtonTap(UIElement sender, int uid) {
		if (uid == 0) {
			MahoMapsApp.GetCanvas().line = null;
			Close();
		}
	}

	private static String Type(int t) {
		switch (t) {
		case YmapsApi.ROUTE_AUTO:
			return "на авто";
		case YmapsApi.ROUTE_BYFOOT:
			return "пешком";
		case YmapsApi.ROUTE_TRANSPORT:
			return "на транспорте";
		}
		return "";
	}

}

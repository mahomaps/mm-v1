package mahomaps.overlays;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import mahomaps.MahoMapsApp;
import mahomaps.api.Http403Exception;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.map.Line;
import mahomaps.map.Route;
import mahomaps.ui.Button;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class RouteOverlay extends MapOverlay implements Runnable, IButtonHandler, CommandListener {

	final Vector v = new Vector();
	private Geopoint a;
	private Geopoint b;
	private final int method;
	Route route;

	private Command leave = new Command("Нет", Command.CANCEL, 0);
	private Command discard = new Command("Да", Command.OK, 0);

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
					new SimpleText(route.distance + ", " + route.time), new Button("Закрыть", 0, this) });
			InvalidateSize();
			MahoMapsApp.GetCanvas().line = new Line(a, route.points);
		} catch (IOException e) {
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Сетевая ошибка."),
					new Button("Ещё раз", 2, this), new Button("Закрыть", 0, this) });
		} catch (Http403Exception e) {
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Отказ в доступе к API."),
					new SimpleText("Сбросьте сессию в меню"), new SimpleText("и повторите попытку."),
					new Button("Ещё раз", 2, this), new Button("Закрыть", 0, this) });
		} catch (Exception e) {
			e.printStackTrace();
			content = new FillFlowContainer(new UIElement[] { new SimpleText("Не удалось построить маршрут."),
					new Button("Закрыть", 1, this) });
			InvalidateSize();
		}
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case 0:
			Alert a1 = new Alert("MahoMaps v1", "Сбросить построенный маршрут?", null, AlertType.WARNING);
			a1.setTimeout(Alert.FOREVER);
			a1.addCommand(discard);
			a1.addCommand(leave);
			a1.setCommandListener(this);
			MahoMapsApp.BringSubScreen(a1);
			break;
		case 1:
			// close after error
			Close();
			break;
		case 2:
			RouteBuildOverlay rbo = RouteBuildOverlay.Get();
			rbo.SetA(a);
			rbo.SetB(b);
			break;
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

	public void commandAction(Command c, Displayable d) {
		if (c == discard) {
			MahoMapsApp.GetCanvas().line = null;
			Close();
		}
		MahoMapsApp.BringMap();
	}

}

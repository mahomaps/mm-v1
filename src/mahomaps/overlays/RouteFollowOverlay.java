package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.route.Route;
import mahomaps.ui.Button;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.UIElement;

public class RouteFollowOverlay extends MapOverlay implements IButtonHandler, CommandListener {

	final Vector v = new Vector();
	private Geopoint a;
	private Geopoint b;
	private int method;
	private Route r;

	private Command leave = new Command("Нет", Command.CANCEL, 0);
	private Command discard = new Command("Да", Command.OK, 0);

	public RouteFollowOverlay(Geopoint a, Geopoint b, int method, Route r) {
		this.a = a;
		this.b = b;
		this.method = method;
		this.r = r;
		content = new FillFlowContainer(new UIElement[] { new Button("Закрыть", 0, this) });
		ShowPoint(null);
	}

	public void ShowPoint(Geopoint p) {
		v.removeAllElements();
		v.addElement(a);
		v.addElement(b);
		if (p != null)
			v.addElement(p);
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

	public void OnButtonTap(UIElement sender, int uid) {
		if (uid == 0) {
			Alert a1 = new Alert("MahoMaps v1", "Сбросить построенный маршрут?", null, AlertType.WARNING);
			a1.setTimeout(Alert.FOREVER);
			a1.addCommand(discard);
			a1.addCommand(leave);
			a1.setCommandListener(this);
			MahoMapsApp.BringSubScreen(a1);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == discard) {
			MahoMapsApp.GetCanvas().line = null;
			MahoMapsApp.route.ReleaseGeolocation(MahoMapsApp.GetCanvas());
			MahoMapsApp.route = null;
			Close();
		}
		MahoMapsApp.BringMap();
	}

}

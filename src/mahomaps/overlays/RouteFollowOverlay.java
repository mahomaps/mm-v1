package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.ui.Button;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.UIElement;

public class RouteFollowOverlay extends MapOverlay implements IButtonHandler, CommandListener {

	final Vector v = new Vector();
	private Geopoint a;
	private Geopoint b;

	public RouteFollowOverlay(Geopoint a, Geopoint b) {
		this.a = a;
		this.b = b;
		content = new FillFlowContainer(new UIElement[] { new Button(MahoMapsApp.text[32], 0, this) });
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

	public boolean CloseButtonImplicit() {
		return false;
	}

	public void OnButtonTap(UIElement sender, int uid) {
		if (uid == 0) {
			Alert a1 = new Alert("MahoMaps v1", MahoMapsApp.text[112], null, AlertType.WARNING);
			a1.setTimeout(Alert.FOREVER);
			a1.addCommand(MahoMapsApp.yes);
			a1.addCommand(MahoMapsApp.no);
			a1.setCommandListener(this);
			MahoMapsApp.BringSubScreen(a1);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.yes) {
			MahoMapsApp.GetCanvas().line = null;
			MahoMapsApp.route.ReleaseGeolocation();
			MahoMapsApp.route = null;
			Close();
		}
		MahoMapsApp.BringMap();
	}

}

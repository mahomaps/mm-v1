package mahomaps.overlays;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.api.Http403Exception;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.map.Line;
import mahomaps.route.Route;
import mahomaps.route.RouteSegment;
import mahomaps.route.RouteTracker;
import mahomaps.screens.MapCanvas;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
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
	private int tries;

	private boolean anchorsShown = false;

	int name = 134;

	public RouteOverlay(Geopoint a, Geopoint b, int method) {
		this.a = a;
		this.b = b;
		this.method = method;
		ShowAB();
		content = new FillFlowContainer(new UIElement[]{new SimpleText(MahoMapsApp.text[134])});
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

	public int GetName() {
		return name;
	}

	public void run() {
		try {
			if (tries != 0) {
				MahoMapsApp.api.RefreshToken();
			}
			// TODO route variant selection
			route = new Route(MahoMapsApp.api.Routes(a, b, method).getObject(0));
			LoadRoute();
			name = 116;
		} catch (IOException e) {
			content = new FillFlowContainer(new UIElement[]{new SimpleText(MahoMapsApp.text[111]),
					new Button(MahoMapsApp.text[37], 2, this), new Button(MahoMapsApp.text[38], 0, this)});
			name = 111;
		} catch (Http403Exception e) {
			if (tries++ == 0) {
				run();
				return;
			}
			content = new FillFlowContainer(
					new UIElement[]{new SimpleText(MahoMapsApp.text[135]), new SimpleText(MahoMapsApp.text[136]),
							new Button(MahoMapsApp.text[37], 2, this), new Button(MahoMapsApp.text[38], 0, this)});
			name = 136;
		} catch (Exception e) {
			e.printStackTrace();
			content = new FillFlowContainer(new UIElement[]{new SimpleText(MahoMapsApp.text[120]),
					new SimpleText(e.getClass().getName()), new Button(MahoMapsApp.text[38], 1, this)});
			name = 120;
		} catch (OutOfMemoryError e) {
			content = new FillFlowContainer(new UIElement[]{new SimpleText(MahoMapsApp.text[121]),
					new Button(MahoMapsApp.text[38], 1, this)});
			name = 121;
		} finally {
			InvalidateSize();
		}
	}

	public void LoadRoute() {
		// route field must be non-null here!
		ColumnsContainer cols = new ColumnsContainer(new UIElement[]{new Button(MahoMapsApp.text[113], 3, this),
				new Button(MahoMapsApp.text[114], 4, this)});
		content = new FillFlowContainer(
				new UIElement[]{new SimpleText(Type(method)), new SimpleText(route.distance + ", " + route.time),
						new Button(MahoMapsApp.text[115], 5, this), cols, new Button(MahoMapsApp.text[38], 0, this)});
		MahoMapsApp.GetCanvas().line = new Line(a, route.points);
		MahoMapsApp.GetCanvas().requestRepaint();
	}

	public void ShowAB() {
		v.removeAllElements();
		v.addElement(a);
		v.addElement(b);
	}

	public void ShowAnchors() {
		v.removeAllElements();
		RouteSegment[] sgs = route.segments;
		for (int i = 0; i < sgs.length; i++) {
			Geopoint p = sgs[i].GetAnchor();
			if (p != null)
				v.addElement(p);
		}
		v.addElement(a);
		v.addElement(b);
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
			case 0: {
				Alert a1 = new Alert("MahoMaps v1", MahoMapsApp.text[112], null, AlertType.WARNING);
				a1.setTimeout(Alert.FOREVER);
				a1.addCommand(MahoMapsApp.yes);
				a1.addCommand(MahoMapsApp.no);
				a1.setCommandListener(this);
				MahoMapsApp.BringSubScreen(a1);
				break;
			}
			case 1:
				// close after error
				Close();
				break;
			case 2:
				RouteBuildOverlay rbo = RouteBuildOverlay.Get();
				rbo.SetA(a);
				rbo.SetB(b);
				break;
			case 3:
				if (route != null) {
					Form f = new Form(MahoMapsApp.text[116]);
					for (int i = 0; i < route.segments.length; i++) {
						Item[] items = route.segments[i].ToLcdui();
						for (int j = 0; j < items.length; j++) {
							items[j].setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
							f.append(items[j]);
						}
					}
					f.addCommand(MahoMapsApp.back);
					f.setCommandListener(this);
					MahoMapsApp.BringSubScreen(f);
				}
				break;
			case 4:
				if (anchorsShown) {
					ShowAB();
				} else {
					ShowAnchors();
				}
				anchorsShown = !anchorsShown;
				break;
			case 5:
				MapCanvas mc = MahoMapsApp.GetCanvas();
				if (mc.geo != null && mc.geo.DrawPoint()) {
					try {
						RouteFollowOverlay rfo = new RouteFollowOverlay(a, b);
						MahoMapsApp.GetCanvas().overlays.PushOverlay(rfo);
						RouteTracker rt = new RouteTracker(route, rfo);
						rt.SpoofGeolocation(MahoMapsApp.GetCanvas());
						MahoMapsApp.route = rt;
						UIElement.Deselect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Alert a1 = new Alert("MahoMaps v1", MahoMapsApp.text[119], null, AlertType.WARNING);
					a1.setTimeout(Alert.FOREVER);
					MahoMapsApp.BringSubScreen(a1, mc);
				}
				break;
		}
	}

	private static String Type(int t) {
		switch (t) {
			case YmapsApi.ROUTE_AUTO:
				return MahoMapsApp.text[126];
			case YmapsApi.ROUTE_BYFOOT:
				return MahoMapsApp.text[127];
			case YmapsApi.ROUTE_TRANSPORT:
				return MahoMapsApp.text[144];
		}
		return "";
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.yes) {
			MahoMapsApp.GetCanvas().line = null;
			Close();
		}
		MahoMapsApp.BringMap();
	}

}

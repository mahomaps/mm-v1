package mahomaps.overlays;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.MahoMapsApp;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.mvp.VehicleCalcs;
import mahomaps.mvp.VisibleVehicle;
import mahomaps.screens.MenuScreen;
import mahomaps.ui.*;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import java.util.Vector;

public class VehiclesOverlay extends MapOverlay implements IButtonHandler, Runnable {
	public static final String ID = "vehicles";
	private final Vector v = new Vector(64);
	private Thread thread;
	int currTime = 0;
	public static boolean isVisible = false;

	public String GetId() {
		return ID;
	}

	public Vector GetPoints() {
		return v;
	}

	public int GetName() {
		return 169;
	}

	public VehiclesOverlay() {
		content = new FillFlowContainer(new UIElement[]{
				new SimpleText(MahoMapsApp.text[169]),
				new Button(MahoMapsApp.text[37], 1, this),
				new Button(MahoMapsApp.text[38], 0, this)});
		Update();
		thread = new Thread(this);
		thread.start();
		isVisible = true;
	}

	public synchronized void Update() {
		Geopoint center = MahoMapsApp.GetCanvas().GetSearchAnchor(false);
		JSONArray a = MahoMapsApp.api.Vehicles(center, 0.04d);
		v.removeAllElements();
		for (int i = 0; i < a.size(); i++) {
			JSONObject obj = a.getObject(i);
			JSONArray coords = obj.getArray("routeSegments").getObject(0).getArray("coordinates").getArray(0);
			double lat = coords.getDouble(1);
			double lon = coords.getDouble(0);
			Geopoint g = new Geopoint(lat, lon);
			VisibleVehicle vv = VisibleVehicle.decode(obj);
			VehicleCalcs.precalcVehicle(vv);
			g.object = vv;
			g.label = obj.getString("name");
			g.type = 1;
			if ("bus".equals(obj.getString("type")))
				g.color = 1;
			else if ("minibus".equals(obj.getString("type")))
				g.color = 3;
			else g.color = 0;
			v.addElement(g);
		}
		currTime = 0;
		((FillFlowContainer) content).children.setElementAt(new SimpleText("Транспорта на карте: " + v.size()), 0);
	}

	public synchronized void UpdatePoints() {
		for (int i = 0; i < v.size(); i++) {
			Geopoint g = (Geopoint) v.elementAt(i);
			VisibleVehicle vv = (VisibleVehicle) g.object;
			double[] next = VehicleCalcs.getVehiclePosition(vv, currTime);
			g.lat = next[1];
			g.lon = next[0];
		}
	}

	public boolean OnPointTap(Geopoint p) {
		if (!(p.object instanceof VisibleVehicle))
			return false;
		VisibleVehicle vv = (VisibleVehicle) p.object;
		JSONObject vehicle = (JSONObject) vv.source;
		JSONObject thread = MahoMapsApp.api.VehicleThread(vehicle.getString("threadId"), vehicle.getString("lineId"), vehicle.getString("id"));
		Form f = new Form(thread.getString("name") + " (" + thread.getString("from") + " - " + thread.getString("to") + ")");
		JSONArray stops = thread.getArray("stops");
		boolean currentFound = false;
		for (int i = 0; i < stops.size(); i++) {
			JSONObject stop = stops.getObject(i);
			String time = stop.getString("ae", null);
			if (!currentFound) {
				if (time != null) {
					for (int j = 0; j < i; j++) {
						StringItem prev = (StringItem) f.get(j);
						prev.setText("Проехал");
					}
					currentFound = true;
				}
			}
			String time2 = time == null ? "Неизвестно" : time;
			StringItem n = new StringItem(stop.getString("n"), time2);
			f.append(n);
		}
		f.addCommand(MahoMapsApp.back);
		f.setCommandListener(new MenuScreen(null));
		MahoMapsApp.BringSubScreen(f);
		return true;
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
			case 0:
				thread.interrupt();
				isVisible = false;
				Close();
				break;
			case 1:
				Update();
				break;
		}
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			UpdatePoints();
			currTime++;
		}
	}
}

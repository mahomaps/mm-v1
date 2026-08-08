package mahomaps.overlays;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.MahoMapsApp;
import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.ui.*;

import java.util.Vector;

public class VehiclesOverlay extends MapOverlay implements IButtonHandler {
	public static final String ID = "vehicles";
	private final Vector v = new Vector(64);

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
	}

	public void Update() {
		Geopoint center = MahoMapsApp.GetCanvas().GetSearchAnchor(false);
		JSONArray a = MahoMapsApp.api.Vehicles(center, 0.3d);
		v.removeAllElements();
		for (int i = 0; i < a.size(); i++) {
			JSONObject obj = a.getObject(i);
			JSONArray coords = obj.getArray("routeSegments").getObject(0).getArray("coordinates").getArray(0);
			double lat = coords.getDouble(1);
			double lon = coords.getDouble(0);
			Geopoint g = new Geopoint(lat, lon);
			g.object = obj.getString("uri");
			g.label = obj.getString("name");
			g.type = 1;
			if ("bus".equals(obj.getString("type")))
				g.color = 1;
			else if ("minibus".equals(obj.getString("type")))
				g.color = 3;
			else g.color = 0;
			v.addElement(g);
		}
		((FillFlowContainer) content).children.setElementAt(new SimpleText("Транспорта на карте: " + v.size()), 0);
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
				Update();
				break;
		}
	}
}

package mahomaps.mvp;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class VisibleVehicle {
	public YGeometry[] routeSegments;
	public int[] routeDurations;
	public JSONObject source;
	public static VisibleVehicle decode(JSONObject j) {
		VisibleVehicle v = new VisibleVehicle();
		JSONArray rsArr = j.getArray("routeSegments");
		JSONArray durArr = j.getArray("routeDurations");
		v.routeSegments = new YGeometry[rsArr.size()];
		v.routeDurations = new int[rsArr.size()];
		for (int i = 0; i < rsArr.size(); i++) {
			v.routeSegments[i] = YGeometry.decode(rsArr.getObject(i));
			v.routeDurations[i] = durArr.getInt(i);
		}
		v.source = j;
		return v;
	}

	// === кеш, заполняется precalcVehicle ===
	public int[] cumTime;          // длина = segments.length + 1
	public double[][] cumDists;    // [seg][point] — кумулятивные длины в метрах
	public boolean precalcDone;
}
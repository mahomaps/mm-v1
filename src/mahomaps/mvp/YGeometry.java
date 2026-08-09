package mahomaps.mvp;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class YGeometry {
	public String type;
	public double[][] coordinates;

	public static YGeometry decode(JSONObject j) {
		YGeometry g = new YGeometry();
		g.type = j.getString("type");
		JSONArray a = j.getArray("coordinates");
		g.coordinates = new double[a.size()][2];
		for (int i = 0; i < a.size(); i++) {
			JSONArray pair = a.getArray(i);
			g.coordinates[i][0] = pair.getDouble(0);
			g.coordinates[i][1] = pair.getDouble(1);
		}
		return g;
	}
}


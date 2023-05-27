package mahomaps.map;

import org.json.me.JSONObject;

public class Route {

	public Geopoint[] points;
	public String time;
	public String distance = "Неизвестно";

	public Route(JSONObject route) {
		JSONObject props = route.getJSONObject("properties");
		JSONObject meta = props.getJSONObject("PathMetaData");
		time = meta.getJSONObject("Duration").getString("text");
		JSONObject dist = meta.optJSONObject("Distance");
		if (dist == null)
			dist = meta.getJSONObject("WalkingDistance");
		if (dist != null)
			distance = dist.getString("text");
		points = RouteDecoder.DecodeRoutePath(props.getString("encodedCoordinates"));
	}

}

package mahomaps.map;

import cc.nnproject.json.JSONObject;

public class Route {

	public Geopoint[] points;
	public String time;
	public String distance = "Неизвестно";

	public Route(JSONObject props) {
		JSONObject meta = props.getObject("PathMetaData");
		time = meta.getObject("Duration").getString("text");
		JSONObject dist = meta.getNullableObject("Distance");
		if (dist == null)
			dist = meta.getObject("WalkingDistance");
		if (dist != null)
			distance = dist.getString("text");
		points = RouteDecoder.DecodeRoutePath(props.getString("encodedCoordinates"));
	}

}

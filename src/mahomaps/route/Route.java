package mahomaps.route;

import cc.nnproject.json.*;

import mahomaps.map.Geopoint;

public class Route {

	public Geopoint[] points;
	public RouteSegment[] segments;
	public String time;
	public String distance = "Неизвестно";

	public Route(JSONObject route) {
		JSONObject props = route.getObject("properties");
		JSONObject meta = props.getObject("PathMetaData");
		time = meta.getObject("Duration").getString("text");
		JSONObject dist = meta.getNullableObject("Distance");
		if (dist == null)
			dist = meta.getObject("WalkingDistance");
		if (dist != null)
			distance = dist.getString("text");
		points = RouteDecoder.DecodeRoutePath(props.getString("encodedCoordinates"));
		segments = RouteDecoder.DecodeSegments(route.getArray("features"), points);
	}

}

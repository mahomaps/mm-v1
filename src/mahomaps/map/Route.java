package mahomaps.map;

import org.json.me.JSONObject;

public class Route {

	public Geopoint[] points;

	public Route(JSONObject props) {
		JSONObject meta = props.getJSONObject("PathMetaData");
		points = RouteDecoder.DecodeRoutePath(props.getString("encodedCoordinates"));
	}

}

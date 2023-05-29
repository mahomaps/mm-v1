package mahomaps.route;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class Route {

	public Geopoint[] points;
	public RouteSegment[] segments;
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
		segments = RouteDecoder.DecodeSegments(route.getJSONArray("features"), points);
		Dump(route);
	}

	public void Dump(JSONObject j) {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(MahoMapsApp.getAppropCachePath() + "route.log");
			if (!fc.exists())
				fc.create();
			else
				fc.truncate(0);
			OutputStream stream = fc.openOutputStream();
			stream.write(j.toString().getBytes("UTF-8"));
			stream.flush();
			stream.close();
		} catch (Exception e) {
		} finally {
			try {
				if (fc != null)
					fc.close();
			} catch (IOException e) {
			}
		}
	}

}

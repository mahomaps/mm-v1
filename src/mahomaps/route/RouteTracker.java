package mahomaps.route;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	Geopoint trueGeolocation = null;
	Geopoint extrapolatedGeolocation = null;
	final Geopoint[] vertex;
	final float[] lineLengths;
	final RouteSegment[] segments;
	int currentSegment;

	public RouteTracker(Route r) {
		vertex = r.points;
		segments = r.segments;
		currentSegment = -2;
		lineLengths = new float[vertex.length - 1];
		for (int i = 0; i < lineLengths.length; i++) {
			lineLengths[i] = Distance(vertex[i], vertex[i + 1]);
		}
	}

	public void SpoofGeolocation(MapCanvas map) {
		trueGeolocation = map.geolocation;
		extrapolatedGeolocation = new Geopoint(trueGeolocation.lat, trueGeolocation.lon);
		extrapolatedGeolocation.type = Geopoint.LOCATION;
		extrapolatedGeolocation.color = Geopoint.COLOR_RED;
		map.geolocation = extrapolatedGeolocation;
	}

	public void ReleaseGeolocation(MapCanvas map) {
		map.geolocation = trueGeolocation;
		trueGeolocation = null;
		extrapolatedGeolocation = null;
	}

	/**
	 * Call this every frame to make tracker work.
	 */
	public void Update() {

	}

	public float Distance(Geopoint a, Geopoint b) {
		double alat = Math.toRadians(a.lat);
		double alon = Math.toRadians(a.lon);
		double blat = Math.toRadians(b.lat);
		double blon = Math.toRadians(b.lon);
		double cosd = Math.sin(alat) * Math.sin(-blat) + Math.cos(alat) * Math.cos(-blat) * Math.cos(alon - blon);

		double d = MahoMapsApp.acos(cosd);
		return (float) (d * 6371000);
	}

}

package mahomaps.route;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.RouteFollowOverlay;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	Geopoint trueGeolocation = null;
	Geopoint extrapolatedGeolocation = null;
	final Geopoint[] vertex;
	final float[] lineLengths;
	final RouteSegment[] segments;
	int currentSegment;

	// drawing temps
	String next;
	String distLeft;
	private RouteFollowOverlay o;

	public RouteTracker(Route r, RouteFollowOverlay o) {
		this.o = o;
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
		extrapolatedGeolocation.lat = trueGeolocation.lat;
		extrapolatedGeolocation.lon = trueGeolocation.lon;
		if (currentSegment == -2) {
			// first update
		} else if (currentSegment == -1) {
			// route start is not reached
		} else {
			// route is follown
		}
		next = "Вернитесь на старт";
		distLeft = "Осталось " + ((Distance(trueGeolocation, vertex[0]))) + "м";
	}

	/**
	 * Call this every frame after {@link #Update()} to draw tracker.
	 */
	public void Draw(Graphics g, int w) {
		g.setColor(MapOverlay.OVERLAY_BG);
		g.fillRoundRect(5, 5, w - 10, 50, 10, 10);
		g.setColor(-1);
		if (next != null)
			g.drawString(next, 10, 10, 0);
		if (distLeft != null)
			g.drawString(distLeft, 10, 30, 0);

	}

	public static float Distance(Geopoint a, Geopoint b) {
		double alat = Math.toRadians(a.lat);
		double alon = Math.toRadians(a.lon);
		double blat = Math.toRadians(b.lat);
		double blon = Math.toRadians(b.lon);
		double cosd = Math.sin(alat) * Math.sin(blat) + Math.cos(alat) * Math.cos(blat) * Math.cos(alon - blon);

		double d = MahoMapsApp.acos(cosd);
		// return d;

		return (float) (d * 6371000D);
	}

}

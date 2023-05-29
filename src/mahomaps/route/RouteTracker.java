package mahomaps.route;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.RouteFollowOverlay;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	private final RouteFollowOverlay o;
	Geopoint trueGeolocation = null;
	Geopoint extrapolatedGeolocation = null;
	private final Geopoint[] vertex;
	private final float[] lineLengths;
	private final RouteSegment[] segments;
	int currentSegment;

	static final int ACCEPTABLE_ERROR = 20;

	// drawing temps
	String next;
	String distLeft;

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
			if (distTo(vertex[0]) < ACCEPTABLE_ERROR) {
				currentSegment = 0;
			} else {
				currentSegment = -1;
			}
		} else if (currentSegment == -1) {
			// route start is not reached
			next = "Вернитесь на старт";
			float d = distTo(vertex[0]);
			distLeft = "Осталось " + ((int) d) + "м";
			if (d < ACCEPTABLE_ERROR) {
				currentSegment = 0;
			}
			o.ShowPoint(segments[0].GetAnchor());
		} else if (currentSegment == segments.length - 1) {
			// last segment
			Geopoint t = vertex[vertex.length - 1];
			float d = distTo(t);
			next = "Финиш";
			distLeft = "Осталось " + ((int) d) + "м";
			if (d < ACCEPTABLE_ERROR) {
				currentSegment++;
			}
			o.ShowPoint(null);
		} else if (currentSegment < segments.length) {
			// route is follown
			RouteSegment s = segments[currentSegment];
			RouteSegment ns = segments[currentSegment + 1];
			next = ns.GetDescription();
			int sv = s.segmentStartVertex;
			int ev = ns.segmentStartVertex;
			float d = ev == 0 ? 292 : distTo(vertex[ev]);
			distLeft = "Осталось " + ((int) d) + "м";
			if (d < ACCEPTABLE_ERROR) {
				currentSegment++;
			}
			o.ShowPoint(ns.GetAnchor());
		} else {
			// route ended
			next = "Маршрут завершён.";
			distLeft = null;
			o.ShowPoint(null);
		}

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

	private float distTo(Geopoint p) {
		return Distance(extrapolatedGeolocation, p);
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

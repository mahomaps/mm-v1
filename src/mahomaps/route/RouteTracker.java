package mahomaps.route;

import javax.microedition.lcdui.Font;
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
	private TrackerOverlayState tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "", "Начинаем маршрут...", "");

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
		}
		if (currentSegment == -1) {
			// route start is not reached
			float d = distTo(vertex[0]);
			tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "Проследуйте к старту",
					"Осталось " + ((int) d) + "м", segments[0].GetDescription());
			o.ShowPoint(segments[0].GetAnchor());
			if (d < ACCEPTABLE_ERROR) {
				currentSegment = 0;
			}
		} else if (currentSegment == segments.length - 1) {
			// last segment
			Geopoint t = vertex[vertex.length - 1];
			float d = distTo(t);
			RouteSegment rs = segments[currentSegment];
			tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, getCurrentSegmentInfo(rs),
					"Через " + ((int) d) + " метров", "Конец маршрута");
			if (d < ACCEPTABLE_ERROR) {
				currentSegment++;
			}
			o.ShowPoint(null);
		} else if (currentSegment < segments.length) {
			// route is follown
			RouteSegment s = segments[currentSegment];
			RouteSegment ns = segments[currentSegment + 1];
			int sv = s.segmentStartVertex;
			int ev = ns.segmentStartVertex;
			// next = ns.GetDescription();
			float d = ev == 0 ? 292 : distTo(vertex[ev]);
			// distLeft = "Осталось " + ((int) d) + "м";
			if (d < ACCEPTABLE_ERROR) {
				currentSegment++;
			}
			o.ShowPoint(ns.GetAnchor());
		} else {
			// route ended
			tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, "", "Маршрут завершён.", "");
			o.ShowPoint(null);
		}

	}

	/**
	 * Call this every frame after {@link #Update()} to draw tracker.
	 */
	public void Draw(Graphics g, int w) {
		Font f = Font.getFont(0, 0, 8);
		int fh = f.getHeight();
		g.setFont(f);
		// bg
		g.setColor(MapOverlay.OVERLAY_BG);
		g.fillRoundRect(5, 5, w - 10, fh * 3 + 10, 10, 10);
		// text
		g.setColor(-1);
		g.drawString(tos.line1, 10, 10, 0);
		g.drawString(tos.line2, 10, 10 + fh, 0);
		g.drawString(tos.line3, 10, 10 + fh + fh, 0);
		// icons
		// TODO

	}

	private static String getCurrentSegmentInfo(RouteSegment rs) {
		if (rs instanceof AutoSegment) {
			AutoSegment as = (AutoSegment) rs;
			if (as.street.length() > 0) {
				return as.street + "; " + as.dist + "м";
			}
			return "Дорога " + as.dist + "м";
		}
		return rs.GetDescription();
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

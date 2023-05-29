package mahomaps.route;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.RouteFollowOverlay;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	public final RouteFollowOverlay overlay;
	Geopoint trueGeolocation = null;
	Geopoint extrapolatedGeolocation = null;
	private final Geopoint[] vertex;
	private final float[] lineLengths;
	private final RouteSegment[] segments;
	int currentSegment;

	static final int ACCEPTABLE_ERROR = 20;

	// drawing temps
	private TrackerOverlayState tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "", "Начинаем маршрут...", "");
	private MapCanvas map;

	public RouteTracker(Route r, RouteFollowOverlay o) {
		this.overlay = o;
		vertex = r.points;
		segments = r.segments;
		currentSegment = -2;
		lineLengths = new float[vertex.length - 1];
		for (int i = 0; i < lineLengths.length; i++) {
			lineLengths[i] = Distance(vertex[i], vertex[i + 1]);
		}
	}

	public void SpoofGeolocation(MapCanvas m) {
		map = m;
		trueGeolocation = map.geolocation;
		extrapolatedGeolocation = new Geopoint(trueGeolocation.lat, trueGeolocation.lon);
		extrapolatedGeolocation.type = Geopoint.LOCATION;
		extrapolatedGeolocation.color = Geopoint.COLOR_RED;
		map.geolocation = extrapolatedGeolocation;
	}

	public void ReleaseGeolocation() {
		map.geolocation = trueGeolocation;
		trueGeolocation = null;
		extrapolatedGeolocation = null;
		map = null;
	}

	/**
	 * Call this every frame to make tracker work.
	 */
	public void Update() {
		extrapolatedGeolocation.lat = trueGeolocation.lat;
		extrapolatedGeolocation.lon = trueGeolocation.lon;
		MapState ms = MapState.FocusAt(extrapolatedGeolocation, map.state.zoom);
		map.state = ms;
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
			final RouteSegment rs = segments[0];
			tos = new TrackerOverlayState(rs.GetIcon(), getSegmentAngle(rs), "Проследуйте к старту",
					"Осталось " + ((int) d) + "м", rs.GetDescription());
			overlay.ShowPoint(rs.GetAnchor());
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
			overlay.ShowPoint(null);
		} else if (currentSegment < segments.length) {
			// route is follown
			RouteSegment s = segments[currentSegment];
			RouteSegment ns = segments[currentSegment + 1];
			int ev = ns.segmentStartVertex;
			float d = distTo(vertex[ev]);
			if (d < 100) {
				final String dist = "Через " + ((int) d) + "м";
				final String a = ns.GetAction();
				final String info = getCurrentSegmentInfo(ns);
				tos = new TrackerOverlayState(ns.GetIcon(), getSegmentAngle(ns), dist, a, info);
			} else {
				final String info = getCurrentSegmentInfo(s);
				final String dist = "Осталось " + ((int) d) + "м";
				final String a = ns.GetAction();
				tos = new TrackerOverlayState(ns.GetIcon(), getSegmentAngle(ns), info, dist, a);
			}
			if (d < ACCEPTABLE_ERROR) {
				currentSegment++;
			}
			overlay.ShowPoint(ns.GetAnchor());
		} else {
			// route ended
			tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, "", "Маршрут завершён.", "");
			overlay.ShowPoint(null);
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

	private static float getSegmentAngle(RouteSegment rs) {
		if (rs instanceof AutoSegment) {
			AutoSegment as = (AutoSegment) rs;
			return (float) as.angle;
		}
		return 0f;
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

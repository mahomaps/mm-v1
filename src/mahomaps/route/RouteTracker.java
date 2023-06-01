package mahomaps.route;

import java.io.IOException;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.RouteFollowOverlay;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	final Image icons;
	public final RouteFollowOverlay overlay;
	Geopoint trueGeolocation = null;
	Geopoint extrapolatedGeolocation = null;
	private final Geopoint[] vertex;
	private final float[] lineLengths;
	private final RouteSegment[] segments;

	int currentSegment;
	int currentVertex;
	boolean anchorTouched;
	boolean trackingLost;

	static final int ANCHOR_TRIGGER_DIST = 20;
	static final int REATTACH_DIST = 40;

	// drawing temps
	private TrackerOverlayState tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "", "Начинаем маршрут...", "");
	private MapCanvas map;

	public RouteTracker(Route r, RouteFollowOverlay o) throws IOException {
		this.overlay = o;
		vertex = r.points;
		segments = r.segments;
		currentSegment = -2;
		lineLengths = new float[vertex.length - 1];
		for (int i = 0; i < lineLengths.length; i++) {
			lineLengths[i] = Distance(vertex[i], vertex[i + 1]);
		}
		icons = Image.createImage("/navigator50.png");
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
			currentVertex = 0;
			if (distTo(vertex[0]) < ANCHOR_TRIGGER_DIST) {
				currentSegment = 0;
			} else {
				currentSegment = -1;
			}
		}
		if (currentSegment == -1) {
			// route start is not reached
			ProcessRouteEntering();
		} else if (currentSegment == segments.length - 1) {
			// last segment
			ProcessLastSegment();
		} else if (currentSegment < segments.length) {
			// route is follown
			ProcessRegularSegment();
		} else {
			// route ended
			tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, "", "Маршрут завершён.", "");
			overlay.ShowPoint(null);
		}

	}

	private void ProcessRouteEntering() {
		final double d = GetDistanceToSegment(vertex[0], vertex[1], extrapolatedGeolocation);
		final RouteSegment rs = segments[0];
		tos = new TrackerOverlayState(rs.GetIcon(), getSegmentAngle(rs), "Проследуйте к старту",
				"Осталось " + ((int) d) + "м", rs.GetDescription());
		overlay.ShowPoint(rs.GetAnchor());
		if (d < REATTACH_DIST) {
			currentSegment = 0;
			currentVertex = 0;
		}
	}

	private void ProcessLastSegment() {
		Geopoint t = vertex[vertex.length - 1];
		float d = distTo(t);
		RouteSegment rs = segments[currentSegment];
		tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, getCurrentSegmentInfo(rs),
				"Через " + ((int) d) + " метров", "Конец маршрута");
		if (d < ANCHOR_TRIGGER_DIST) {
			currentSegment++;
		}
		overlay.ShowPoint(null);
	}

	private void ProcessRegularSegment() {
		if (trackingLost) {
			if (!TryReattach()) {
				// if could not reattach, we are off route and nothing to process
				return;
			}
			trackingLost = false;
			// voice returning to route

			if (currentSegment == segments.length - 1) {
				// last segment.
				ProcessLastSegment();
				return;
			}

			// processing our segment as usual
		}
		RouteSegment s = segments[currentSegment];
		RouteSegment ns = segments[currentSegment + 1];
		int ev = ns.segmentStartVertex;
		float d = distTo(vertex[ev]);
		if (d < 200) {
			final String dist = "Через " + ((int) d) + "м";
			final String a = ns.GetAction();
			final String info = getCurrentSegmentInfo(ns);
			tos = new TrackerOverlayState(ns.GetIcon(), getSegmentAngle(ns), dist, a, info);
		} else {
			final String info = getCurrentSegmentInfo(s);
			final String dist = "Осталось " + ((int) d) + "м";
			final String a = ns.GetAction();
			tos = new TrackerOverlayState(s.GetIcon(), 0f, info, dist, a);
		}
		{
			double distToThis = GetDistanceToSegment(vertex[currentVertex], vertex[currentVertex + 1],
					extrapolatedGeolocation);
			double distToNext = GetDistanceToSegment(vertex[currentVertex + 1], vertex[currentVertex + 2],
					extrapolatedGeolocation);
			if (distToNext < ANCHOR_TRIGGER_DIST) {
				// we are close enough to next line
				currentVertex++;
			} else if (distToThis < REATTACH_DIST) {
				// everything is okay, we are moving along the line
				// do nothing
			} else {
				// tracking is lost! Reattaching.
				if (TryReattach()) {
					// sucseed.
				} else {
					trackingLost = true;
					tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "", "Уход с маршрута", "");
					overlay.ShowPoint(null);
				}
			}
		}
		if (anchorTouched) {
			if (GetDistanceToSegment(vertex[ev - 1], vertex[ev], extrapolatedGeolocation) > ANCHOR_TRIGGER_DIST) {
				currentSegment++;
				anchorTouched = false;
			}
		} else if (d < ANCHOR_TRIGGER_DIST) {
			anchorTouched = true;
			// voice the action
		}
		overlay.ShowPoint(ns.GetAnchor());
	}

	/**
	 * Пытается присоединиться к сегменту линии маршрута. Текущий
	 * сегмент/вершина/tos в случае успеха будут изменены на подходящие.
	 * 
	 * @return True, если удалось.
	 */
	private boolean TryReattach() {
		int found = -1;
		for (int i = Math.max(currentVertex - 2, 0); i < vertex.length - 1; i++) {
			double dist = GetDistanceToSegment(vertex[i], vertex[i + 1], extrapolatedGeolocation);
			if (dist < REATTACH_DIST) {
				found = i;
				break;
			}
		}
		if (found == -1)
			return false;

		currentVertex = found;
		for (int i = currentSegment; i < segments.length; i++) {
			if (currentVertex > segments[i].segmentStartVertex) {
				currentSegment = i - 1;
				break;
			}
		}
		currentSegment = segments.length - 1;
		tos = new TrackerOverlayState(RouteSegment.NO_ICON, 0, "", "Возврат на маршрут...", "");
		overlay.ShowPoint(null);
		return true;
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
		int x = tos.icon == RouteSegment.NO_ICON ? 10 : (10 + 5 + 50);
		g.setColor(-1);
		g.drawString(tos.line1, x, 10, 0);
		g.drawString(tos.line2, x, 10 + fh, 0);
		g.drawString(tos.line3, x, 10 + fh + fh, 0);
		// icons
		int cx = 10 + 25;
		int cy = 10 + fh + fh / 2;
		if (tos.icon == RouteSegment.MANEUVER_ANGLE) {
			g.setColor(-1);
			final int THICK = 6;
			final int ARROW = 9;
			g.fillRoundRect(cx - THICK / 2, cy - THICK / 2, THICK, 25 + THICK / 2, THICK, THICK);
			float sin = (float) Math.sin(Math.toRadians(tos.angle));
			float cos = (float) Math.cos(Math.toRadians(tos.angle));
			{
				// якоря
				int x25 = (int) (sin * 25);
				int y25 = (int) (cos * 25);
				final float x25t = (sin * (25 - ARROW));
				final float y25t = (cos * (25 - ARROW));

				// оффсеты для линии
				float ldx = (cos * (THICK / 2));
				float ldy = (-sin * (THICK / 2));
				float adx = (cos * ARROW);
				float ady = (-sin * ARROW);
				// стрелка
				int xAl = (int) (cx - x25t - adx);
				int yAl = (int) (cy - y25t - ady);
				int xAr = (int) (cx - x25t + adx);
				int yAr = (int) (cy - y25t + ady);
				// углы линии
				int lfblx = (int) (cx - ldx);
				int lfbly = (int) (cy - ldy);
				int lfbrx = (int) (cx + ldx);
				int lfbry = (int) (cy + ldy);
				int lftlx = (int) (cx - x25t - ldx);
				int lftly = (int) (cy - y25t - ldy);
				int lftrx = (int) (cx - x25t + ldx);
				int lftry = (int) (cy - y25t + ldy);

				g.fillTriangle(lfblx, lfbly, lfbrx, lfbry, lftlx, lftly);
				g.fillTriangle(lftrx, lftry, lfbrx, lfbry, lftlx, lftly);
				g.fillTriangle(cx - x25, cy - y25, xAl, yAl, xAr, yAr);
			}
		} else if (tos.icon != RouteSegment.NO_ICON) {
			g.drawRegion(icons, (tos.icon - 1) * 50, 0, 50, 50, 0, cx, cy, Graphics.VCENTER | Graphics.HCENTER);
		}
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

	public static double GetDistanceToSegment(Geopoint a, Geopoint b, Geopoint point) {
		double degDist = GetDistanceToSegment(a.lon, a.lat, b.lon, b.lat, point.lon, point.lat);
		Geopoint p = new Geopoint(point.lat + degDist, point.lon);
		return Distance(point, p);
	}

	public static double GetDistanceToSegment(double ax, double ay, double bx, double by, double x, double y) {
		double ak = Math.sqrt((x - ax) * (x - ax) + (y - ay) * (y - ay));
		double kb = Math.sqrt((x - bx) * (x - bx) + (y - by) * (y - by));
		double ab = Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by));

		// скалярное произведение векторов
		double mulScalarAKAB = (x - ax) * (bx - ax) + (y - ay) * (by - ay);
		double mulScalarBKAB = (x - bx) * (-bx + ax) + (y - by) * (-by + ay);

		if (ab == 0)
			return ak;

		else if (mulScalarAKAB >= 0 && mulScalarBKAB >= 0) {

			double p = (ak + kb + ab) / 2.0;
			double s = Math.sqrt(Math.abs((p * (p - ak) * (p - kb) * (p - ab))));

			return (2.0 * s) / ab;
		}

		else if (mulScalarAKAB < 0 || mulScalarBKAB < 0) {
			return Math.min(ak, kb);
		}

		else
			return 0;

	}

}

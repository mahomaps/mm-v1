package mahomaps.route;

import java.io.IOException;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.map.GeoUpdateThread;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.overlays.MapOverlay;
import mahomaps.overlays.RouteFollowOverlay;
import mahomaps.screens.MapCanvas;

public class RouteTracker {

	final Image icons;
	public final RouteFollowOverlay overlay;
	Geopoint trueGeolocation = null;
	GeoUpdateThread geoProvider = null;
	Geopoint extrapolatedGeolocation = null;
	private final Geopoint[] vertex;
	private final float[] lineLengths;
	private final RouteSegment[] segments;

	int currentSegment;
	int currentVertex;
	boolean anchorTouched;
	boolean trackingLost;
	private long lastUpdateTime = 0;
	private int lastUpdateNumber;
	// look at https://t.me/nnmidletschat/13309 for details
	private double lat1, lon1, lat2, lon2, lat3, lon3, timeDelta12;

	static final int ANCHOR_TRIGGER_DIST = 20;
	static final int REATTACH_DIST = 50;

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
		geoProvider = map.geo;
		trueGeolocation = map.geolocation;
		extrapolatedGeolocation = new Geopoint(trueGeolocation.lat, trueGeolocation.lon);
		extrapolatedGeolocation.type = Geopoint.LOCATION;
		extrapolatedGeolocation.color = Geopoint.COLOR_RED;
		map.geolocation = extrapolatedGeolocation;
	}

	// sync to avoid detaching during update
	public synchronized void ReleaseGeolocation() {
		map.geolocation = trueGeolocation;
		trueGeolocation = null;
		extrapolatedGeolocation = null;
		map = null;
		geoProvider = null;
	}

	/**
	 * Call this every frame to make tracker work.
	 */
	public synchronized void Update() {
		if (map == null) {
			// we are detached from map
			return;
		}
		if (lastUpdateTime == 0) {
			// init
			lastUpdateNumber = geoProvider.updateCount;
			lastUpdateTime = System.currentTimeMillis();
			lat1 = lat2 = lat3 = trueGeolocation.lat;
			lon1 = lon2 = lon3 = trueGeolocation.lon;
		} else {
			if (lastUpdateNumber != geoProvider.updateCount)
				ProcessGeoUpdate();
			ProcessGeo();
		}

		MapState ms = MapState.FocusAt(extrapolatedGeolocation, map.state.zoom);
		map.state = ms;
		map.line.drawFrom = currentVertex - 1;
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
		} else if (currentSegment < segments.length) {
			// route is follown
			ProcessRegularSegment();
		} else {
			// route ended
			tos = new TrackerOverlayState(RouteSegment.ICON_FINISH, 0, "", MahoMapsApp.text[140], "");
			overlay.ShowPoint(null);
		}

	}

	private void ProcessGeoUpdate() {
		final long now = System.currentTimeMillis();
		timeDelta12 = (now - lastUpdateTime) / 1000d;
		lat1 = lat2;
		lon1 = lon2;
		lat2 = trueGeolocation.lat;
		lon2 = trueGeolocation.lon;
		lat3 = extrapolatedGeolocation.lat;
		lon3 = extrapolatedGeolocation.lon;
		lastUpdateTime = now;
		lastUpdateNumber = geoProvider.updateCount;
	}

	private void ProcessGeo() {
		final long now = System.currentTimeMillis();
		final double delta = (now - lastUpdateTime) / 1000d;
		double prg = (delta / timeDelta12) + 1d;
		if (prg > 2d)
			prg = 2d;
		double tlon = lerp(lon1, lon2, prg);
		double tlat = lerp(lat1, lat2, prg);
		if (delta < 1d) {
			tlon = lerp(lon3, tlon, delta);
			tlat = lerp(lat3, tlat, delta);
		}
		extrapolatedGeolocation.lat = tlat;
		extrapolatedGeolocation.lon = tlon;
	}

	private static double lerp(double a, double b, double f) {
		return a * (1.0 - f) + (b * f);
	}

	private void ProcessRouteEntering() {
		final double d = GetDistanceToSegment(vertex[0], vertex[1], extrapolatedGeolocation);
		final RouteSegment rs = segments[0];
		tos = new TrackerOverlayState(rs.GetIcon(), getSegmentAngle(rs), MahoMapsApp.text[143],
				"Осталось " + ((int) d) + "м", rs.GetDescription());
		overlay.ShowPoint(rs.GetAnchor());
		if (TryReattach()) {
			// segment/vertex are set, do nothing
		}
	}

	private void ProcessRegularSegment() {
		if (trackingLost) {
			if (!TryReattach()) {
				// if could not reattach, we are off route and nothing to process
				return;
			}
			trackingLost = false;
			map.line.drawFrom = currentVertex - 1;
			map.line.Invalidate();
			// voice returning to route

			// processing our segment as usual
		}
		RouteSegment s = segments[currentSegment];
		RouteSegment ns;
		int ev;
		String na;
		if (currentSegment == segments.length - 1) {
			ns = null;
			ev = vertex.length - 1;
			na = MahoMapsApp.text[141];
		} else {
			ns = segments[currentSegment + 1];
			ev = ns.segmentStartVertex;
			na = ns.GetAction();
		}

		float d = distTo(vertex[ev]);
		if (d < 200) {
			final String dist = "Через " + ((int) d) + "м";
			final String info = ns == null ? "" : getCurrentSegmentInfo(ns);
			final int icon = ns == null ? RouteSegment.ICON_FINISH : ns.GetIcon();
			tos = new TrackerOverlayState(icon, getSegmentAngle(ns), dist, na, info);
		} else {
			final String info = getCurrentSegmentInfo(s);
			final String dist = "Осталось " + ((int) d) + "м";
			tos = new TrackerOverlayState(s.GetIcon(), 0f, info, dist, na);
		}
		{
			double distToThis = GetDistanceToSegment(vertex[currentVertex], vertex[currentVertex + 1],
					extrapolatedGeolocation);
			double distToNext = GetDistanceToSegment(vertex[currentVertex + 1], vertex[currentVertex + 2],
					extrapolatedGeolocation);
			if (distToNext < ANCHOR_TRIGGER_DIST) {
				// we are close enough to next line
				currentVertex++;
				map.line.drawFrom = currentVertex - 1;
				map.line.Invalidate();
			} else if (distToThis < REATTACH_DIST) {
				// everything is okay, we are moving along the line
				// do nothing
			} else {
				// tracking is lost! Reattaching.
				if (TryReattach()) {
					map.line.drawFrom = currentVertex - 1;
					map.line.Invalidate();
					// sucseed.
				} else {
					trackingLost = true;
					tos = new TrackerOverlayState(RouteSegment.ICON_WALK, 0, "", MahoMapsApp.text[142], "");
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
		overlay.ShowPoint(ns == null ? null : ns.GetAnchor());
	}

	/**
	 * Пытается присоединиться к сегменту линии маршрута. Текущий сегмент/вершина в
	 * случае успеха будут изменены на подходящие.
	 * 
	 * @return True, если удалось.
	 */
	private boolean TryReattach() {
		int found = -1;
		for (int i = Math.max(currentVertex - 5, 0); i < vertex.length - 1; i++) {
			double dist = GetDistanceToSegment(vertex[i], vertex[i + 1], extrapolatedGeolocation);
			if (dist < REATTACH_DIST) {
				found = i;
				break;
			}
		}
		if (found == -1)
			return false;

		currentVertex = found;

		for (int i = segments.length - 1; i >= 0; i--) {
			int sv = segments[i].segmentStartVertex;
			if (currentVertex >= sv) {
				currentSegment = i;
				break;
			}
		}

		return true;
	}

	/**
	 * Call this every frame after {@link #Update()} to draw tracker. May fail due
	 * to corrupted state object.
	 */
	public void Draw(Graphics g, int w) {
		try {
			drawInternal(g, w);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to redraw route tracker: " + e.toString());
		}
	}

	private final void drawInternal(Graphics g, int w) {
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

	/**
	 * @return Null if rs was null, segment action angle if it's auto, 0 if not.
	 */
	private static float getSegmentAngle(RouteSegment rs) {
		if (rs == null)
			return 0f;
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

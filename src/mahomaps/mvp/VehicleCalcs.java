package mahomaps.mvp;

import mahomaps.MahoMapsApp;

public class VehicleCalcs {
	public static void precalcVehicle(VisibleVehicle v) {
		if (v == null || v.routeSegments == null || v.routeDurations == null) {
			return;
		}
		int n = v.routeSegments.length;
		if (n == 0 || n != v.routeDurations.length) {
			v.precalcDone = false;
			return;
		}

		int[] cumTime = new int[n + 1];
		cumTime[0] = 0;
		for (int i = 0; i < n; i++) {
			int d = v.routeDurations[i];
			if (d < 0) d = 0;
			cumTime[i + 1] = cumTime[i] + d;
		}
		v.cumTime = cumTime;

		double[][] cumDists = new double[n][];
		for (int i = 0; i < n; i++) {
			double[][] coords = v.routeSegments[i].coordinates;
			if (coords == null || coords.length == 0) {
				cumDists[i] = new double[0];
				continue;
			}
			double[] cd = new double[coords.length];
			cd[0] = 0.0;
			for (int j = 1; j < coords.length; j++) {
				cd[j] = cd[j - 1] + distMeters(coords[j - 1], coords[j]);
			}
			cumDists[i] = cd;
		}
		v.cumDists = cumDists;
		v.precalcDone = true;
	}

	// ===== get position =====
// Возвращает double[3] = { lat, lon, angleDegrees }
// angle: 0 = North, по часовой стрелке (East = 90, South = 180, West = 270)
	public static double[] getVehiclePosition(VisibleVehicle v, int currentTime) {
		if (v == null) return null;
		if (!v.precalcDone || v.cumTime == null || v.cumDists == null) {
			precalcVehicle(v);
		}

		int[] cumTime = v.cumTime;
		int n = cumTime.length - 1;
		if (n <= 0) {
			return null;
		}

		// До начала маршрута
		if (currentTime <= 0) {
			double[][] coords = v.routeSegments[0].coordinates;
			return makeResult(coords, 0, 0.0);
		}

		// После конца маршрута
		if (currentTime >= cumTime[n]) {
			double[][] coords = v.routeSegments[n - 1].coordinates;
			int last = coords.length - 2;
			if (last < 0) last = 0;
			return makeResult(coords, last, 1.0);
		}

		// Ищем сегмент
		int seg = 0;
		for (int i = 0; i < n; i++) {
			if (currentTime < cumTime[i + 1]) {
				seg = i;
				break;
			}
		}

		int dur = v.routeDurations[seg];
		if (dur <= 0) {
			double[][] coords = v.routeSegments[seg].coordinates;
			return makeResult(coords, 0, 0.0);
		}

		double frac = (double) (currentTime - cumTime[seg]) / (double) dur;
		if (frac < 0.0) frac = 0.0;
		if (frac > 1.0) frac = 1.0;

		double[][] coords = v.routeSegments[seg].coordinates;
		double[] cd = v.cumDists[seg];

		if (coords == null || coords.length == 0) {
			return null;
		}
		if (coords.length == 1) {
			return new double[]{coords[0][0], coords[0][1], 0.0};
		}

		double totalD = cd[cd.length - 1];
		if (totalD <= 0.0) {
			return makeResult(coords, 0, 0.0);
		}

		double targetD = frac * totalD;

		// Находим ребро
		int j = 0;
		for (int i = 1; i < cd.length; i++) {
			if (targetD <= cd[i]) {
				j = i - 1;
				break;
			}
			j = i - 1;
		}

		double edgeLen = cd[j + 1] - cd[j];
		double f = (edgeLen > 0.0) ? (targetD - cd[j]) / edgeLen : 0.0;
		if (f < 0.0) f = 0.0;
		if (f > 1.0) f = 1.0;

		return makeResult(coords, j, f);
	}

// ===== вспомогательные =====

	private static double[] makeResult(double[][] coords, int edgeIdx, double f) {
		if (coords == null || coords.length == 0) {
			return null;
		}
		if (coords.length == 1) {
			return new double[]{coords[0][0], coords[0][1], 0.0};
		}
		if (edgeIdx < 0) edgeIdx = 0;
		if (edgeIdx >= coords.length - 1) edgeIdx = coords.length - 2;

		double[] p1 = coords[edgeIdx];
		double[] p2 = coords[edgeIdx + 1];
		double[] pos = interpolate(p1, p2, f);
		double angle = bearingDegrees(p1, p2);
		return new double[]{pos[0], pos[1], angle};
	}

	private static double distMeters(double[] a, double[] b) {
		// a, b = [lat, lon]
		double dLat = (b[0] - a[0]) * 111320.0;
		double avgLat = (a[0] + b[0]) * 0.5;
		double dLon = (b[1] - a[1]) * 111320.0 * Math.cos(Math.toRadians(avgLat));
		return Math.sqrt(dLat * dLat + dLon * dLon);
	}

	private static double[] interpolate(double[] p1, double[] p2, double f) {
		return new double[]{
				p1[0] + f * (p2[0] - p1[0]),
				p1[1] + f * (p2[1] - p1[1])
		};
	}

	// Азимут: 0 = North, по часовой (East=90 …)
	private static double bearingDegrees(double[] p1, double[] p2) {
		double lat1 = Math.toRadians(p1[0]);
		double lon1 = Math.toRadians(p1[1]);
		double lat2 = Math.toRadians(p2[0]);
		double lon2 = Math.toRadians(p2[1]);

		double dLon = lon2 - lon1;
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) -
				Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		double brng = Math.toDegrees(MahoMapsApp.atan2(y, x));
		if (brng < 0.0) brng += 360.0;
		if (brng >= 360.0) brng -= 360.0;
		return brng;
	}
}

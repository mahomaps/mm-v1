package mahomaps.route;

import mahomaps.map.Geopoint;

public class AutoSegment extends RouteSegment {

	private String descr;
	private int dist;
	private double angle;
	private int duration;
	private String actionKey;
	private Geopoint actionPoint;

	public AutoSegment(String descr, int dist, double angle, int duration, String actionKey, Geopoint actionPoint) {
		this.descr = descr;
		this.dist = dist;
		this.angle = angle;
		this.duration = duration;
		this.actionKey = actionKey;
		this.actionPoint = new Geopoint(actionPoint.lat, actionPoint.lon);
	}

	public Geopoint GetAnchor() {
		return actionPoint;
	}

	public int GetDistance() {
		return dist;
	}

	public String GetIcon() {
		return null;
	}

	public String GetType() {
		return "На автомобиле";
	}

	public String GetDescription() {
		return descr + ", " + dist + " метров";
	}

}

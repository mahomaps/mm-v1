package mahomaps.route;

import mahomaps.map.Geopoint;

public class AutoSegment extends RouteSegment {

	private String descr;
	private int dist;
	private double angle;
	private int duration;
	private String actionKey;
	private Geopoint actionPoint;
	private int actionVertex;

	public AutoSegment(String descr, int dist, double angle, int duration, String actionKey, int actionVertex,
			Geopoint actionPoint) {
		this.descr = descr;
		this.dist = dist;
		this.angle = angle;
		this.duration = duration;
		this.actionKey = actionKey;
		this.actionVertex = actionVertex;
		this.actionPoint = new Geopoint(actionPoint.lat, actionPoint.lon);
		this.actionPoint.type = Geopoint.POI_MARK;
		this.actionPoint.color = Geopoint.COLOR_BLUE;
	}

	public Geopoint GetAnchor() {
		return actionPoint;
	}

	public int GetSegmentStartVertex() {
		return actionVertex;
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

package mahomaps.route;

import mahomaps.map.Geopoint;

public class AutoSegment extends RouteSegment {

	public final String descr;
	public final int dist;
	public final double angle;
	public final int duration;
	public final String actionKey;
	public final Geopoint actionPoint;
	public final String street;
	public final String actionText;

	public AutoSegment(String descr, String street, int dist, double angle, int duration, String actionKey,
			String actionText, int actionVertex, Geopoint actionPoint) {
		super(actionVertex);
		this.descr = descr;
		this.street = street;
		this.dist = dist;
		this.angle = angle;
		this.duration = duration;
		this.actionKey = actionKey;
		if (actionText == null || actionText.length() == 0) {
			this.actionText = "";
		} else {
			this.actionText = Character.toUpperCase(actionText.charAt(0)) + actionText.substring(1);
		}
		this.actionPoint = new Geopoint(actionPoint.lat, actionPoint.lon);
		this.actionPoint.type = Geopoint.POI_MARK;
		this.actionPoint.color = Geopoint.COLOR_BLUE;
	}

	public Geopoint GetAnchor() {
		return actionPoint;
	}

	public int GetDistance() {
		return dist;
	}

	public int GetIcon() {
		return MANEUVER_ANGLE;
	}

	public String GetType() {
		return "На автомобиле";
	}

	public String GetDescription() {
		return descr + ", " + dist + " метров";
	}

	public String GetAction() {
		return actionText;
	}

}

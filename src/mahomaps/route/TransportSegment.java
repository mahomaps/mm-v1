package mahomaps.route;

import mahomaps.map.Geopoint;

public class TransportSegment extends RouteSegment {

	private String descr;
	private final Geopoint anchor;

	public TransportSegment(String descr, int sv, Geopoint anchor) {
		super(sv);
		this.descr = descr;
		this.anchor = new Geopoint(anchor.lat, anchor.lon);
		this.anchor.type = Geopoint.POI_MARK;
		this.anchor.color = Geopoint.COLOR_BLUE;
	}

	public int GetDistance() {
		return 0;
	}

	public int GetIcon() {
		return ICON_BUS;
	}

	public String GetType() {
		return "На транспорте";
	}

	public String GetDescription() {
		return descr;
	}

	public String GetAction() {
		return "Сядьте на транспорт";
	}

	public Geopoint GetAnchor() {
		return anchor;
	}
}

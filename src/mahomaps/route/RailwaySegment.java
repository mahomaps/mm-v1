package mahomaps.route;

import mahomaps.map.Geopoint;

public class RailwaySegment extends TransportSegment {

	public RailwaySegment(String descr, int sv, Geopoint a) {
		super(descr, sv, a);
	}

	public String GetType() {
		return "На пригородном поезде";
	}

	public int GetIcon() {
		return ICON_SUBURBAN;
	}

}

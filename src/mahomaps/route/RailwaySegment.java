package mahomaps.route;

public class RailwaySegment extends TransportSegment {

	public RailwaySegment(String descr, int sv) {
		super(descr, sv);
	}

	public String GetType() {
		return "На пригородном поезде";
	}

	public int GetIcon() {
		return ICON_SUBURBAN;
	}

}

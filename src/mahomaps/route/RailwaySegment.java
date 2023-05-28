package mahomaps.route;

public class RailwaySegment extends TransportSegment {

	public RailwaySegment(String descr) {
		super(descr);
	}

	public String GetType() {
		return "На пригородном поезде";
	}

}

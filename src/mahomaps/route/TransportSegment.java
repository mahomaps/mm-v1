package mahomaps.route;

public class TransportSegment extends RouteSegment {

	private String descr;

	public TransportSegment(String descr) {
		this.descr = descr;
	}

	public int GetDistance() {
		return 0;
	}

	public String GetIcon() {
		return null;
	}

	public String GetType() {
		return "На транспорте";
	}

	public String GetDescription() {
		return descr;
	}

}

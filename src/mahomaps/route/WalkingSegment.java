package mahomaps.route;

public class WalkingSegment extends RouteSegment {

	private String descr;
	private int dist;

	public WalkingSegment(String descr, int dist) {
		this.descr = descr;
		this.dist = dist;
	}

	public int GetDistance() {
		return dist;
	}

	public String GetIcon() {
		return null;
	}

	public String GetType() {
		return "Пешком";
	}

	public String GetDescription() {
		return descr;
	}

}

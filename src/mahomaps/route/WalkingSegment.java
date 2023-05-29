package mahomaps.route;

public class WalkingSegment extends RouteSegment {

	private String descr;
	private int dist;

	public WalkingSegment(String descr, int dist, int sv) {
		super(sv);
		this.descr = descr;
		this.dist = dist;
	}

	public int GetDistance() {
		return dist;
	}

	public int GetIcon() {
		return ICON_WALK;
	}

	public String GetType() {
		return "Пешком";
	}

	public String GetDescription() {
		return descr;
	}

	public String GetAction() {
		return "Идите пешком";
	}

}

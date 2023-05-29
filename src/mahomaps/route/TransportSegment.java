package mahomaps.route;

public class TransportSegment extends RouteSegment {

	private String descr;

	public TransportSegment(String descr, int sv) {
		super(sv);
		this.descr = descr;
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

}

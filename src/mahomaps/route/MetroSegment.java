package mahomaps.route;

public class MetroSegment extends TransportSegment {

	public MetroSegment(String descr, int sv) {
		super(descr, sv);
	}

	public String GetType() {
		return "На метро";
	}

	public int GetIcon() {
		return ICON_METRO;
	}
}

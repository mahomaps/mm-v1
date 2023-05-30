package mahomaps.route;

import mahomaps.map.Geopoint;

public class MetroSegment extends TransportSegment {

	public MetroSegment(String descr, int sv, Geopoint a) {
		super(descr, sv, a);
	}

	public String GetType() {
		return "На метро";
	}

	public int GetIcon() {
		return ICON_METRO;
	}
}

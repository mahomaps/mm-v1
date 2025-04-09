/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.route;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class MetroSegment extends TransportSegment {

	public MetroSegment(String descr, int sv, Geopoint a) {
		super(descr, sv, a);
	}

	public String GetType() {
		return MahoMapsApp.text[138];
	}

	public int GetIcon() {
		return ICON_METRO;
	}
}

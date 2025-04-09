/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.route;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class RailwaySegment extends TransportSegment {

	public RailwaySegment(String descr, int sv, Geopoint a) {
		super(descr, sv, a);
	}

	public String GetType() {
		return MahoMapsApp.text[139];
	}

	public int GetIcon() {
		return ICON_SUBURBAN;
	}

}

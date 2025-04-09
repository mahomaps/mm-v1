/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.route;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class TransportSegment extends RouteSegment {

	private String descr;
	private final Geopoint anchor;

	public TransportSegment(String descr, int sv, Geopoint anchor) {
		super(sv);
		this.descr = descr;
		this.anchor = new Geopoint(anchor.lat, anchor.lon);
		this.anchor.type = Geopoint.POI_MARK;
		this.anchor.color = Geopoint.COLOR_BLUE;
	}

	public int GetDistance() {
		return 0;
	}

	public int GetIcon() {
		return ICON_BUS;
	}

	public String GetType() {
		return MahoMapsApp.text[144];
	}

	public String GetDescription() {
		return descr;
	}

	public String GetAction() {
		return MahoMapsApp.text[145];
	}

	public Geopoint GetAnchor() {
		return anchor;
	}
}

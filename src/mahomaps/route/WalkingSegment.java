/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.route;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class WalkingSegment extends RouteSegment {

	private final String descr;
	private final int dist;
	private final Geopoint anchor;

	public WalkingSegment(String descr, int dist, int sv, Geopoint anchor) {
		super(sv);
		this.descr = descr;
		this.dist = dist;
		this.anchor = new Geopoint(anchor.lat, anchor.lon);
		this.anchor.type = Geopoint.POI_MARK;
		this.anchor.color = Geopoint.COLOR_BLUE;
	}

	public int GetDistance() {
		return dist;
	}

	public int GetIcon() {
		return ICON_WALK;
	}

	public String GetType() {
		return MahoMapsApp.text[127];
	}

	public String GetDescription() {
		return descr;
	}

	public String GetAction() {
		return MahoMapsApp.text[147];
	}

	public Geopoint GetAnchor() {
		return anchor;
	}
}

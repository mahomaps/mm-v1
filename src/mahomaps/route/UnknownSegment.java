package mahomaps.route;

import mahomaps.MahoMapsApp;

public final class UnknownSegment extends RouteSegment {

	protected UnknownSegment(int sv) {
		super(sv);
	}

	public int GetDistance() {
		return 0;
	}

	public int GetIcon() {
		return NO_ICON;
	}

	public String GetType() {
		return MahoMapsApp.text[146];
	}

	public String GetDescription() {
		return MahoMapsApp.text[146];
	}

	public String GetAction() {
		return MahoMapsApp.text[146];
	}

}

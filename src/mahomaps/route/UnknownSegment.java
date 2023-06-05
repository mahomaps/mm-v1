package mahomaps.route;

public class UnknownSegment extends RouteSegment {

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
		return "Неизвестный сегмент";
	}

	public String GetDescription() {
		return "Неизвестный сегмент";
	}

	public String GetAction() {
		return "Неизвестный сегмент";
	}

}

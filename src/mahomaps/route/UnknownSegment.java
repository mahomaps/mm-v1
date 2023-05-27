package mahomaps.route;

public class UnknownSegment extends RouteSegment {

	public int GetDistance() {
		return 0;
	}

	public String GetIcon() {
		return null;
	}

	public String GetType() {
		return "Неизвестный сегмент";
	}

	public String GetDescription() {
		return "Неизвестный сегмент";
	}

}

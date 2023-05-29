package mahomaps.route;

public class TrackerOverlayState {
	public final int icon;
	public final float angle;
	public final String line1;
	public final String line2;
	public final String line3;

	public TrackerOverlayState(int icon, float angle, String line1, String line2, String line3) {
		if (line1 == null)
			throw new NullPointerException();
		if (line2 == null)
			throw new NullPointerException();
		if (line3 == null)
			throw new NullPointerException();
		this.icon = icon;
		this.angle = angle;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
	}
}

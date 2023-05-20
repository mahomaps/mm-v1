package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.map.GeoUpdateThread;
import mahomaps.screens.MapCanvas;

public class GeolocationButton extends ControlButton {

	private final MapCanvas map;

	public GeolocationButton(MapCanvas map) {
		super(4, null, 0);
		this.map = map;
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		if (map.geo == null) {
			n = 4;
		} else {
			switch (map.geo.state) {
			case GeoUpdateThread.STATE_PENDING:
			case GeoUpdateThread.STATE_OK_PENDING:
				n = 5;
				break;
			case GeoUpdateThread.STATE_OK:
				n = 6;
				break;
			default:
				n = 7;
				break;
			}
		}
		super.Paint(g, x, y, w, h);
	}

	public void OnTap() {
		map.ShowGeo();
	}
}

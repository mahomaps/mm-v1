/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.GeoUpdateThread;
import mahomaps.screens.MapCanvas;

public class GeolocationButton extends ControlButton {

	private final MapCanvas map;

	private boolean hasGeo;

	public GeolocationButton(MapCanvas map) {
		super(4, null, 0);
		this.map = map;
		try {
			Class.forName("javax.microedition.location.LocationProvider");
			hasGeo = true;
		} catch (Throwable e) {
			hasGeo = false;
		}
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
		if (hasGeo && MahoMapsApp.route == null)
			super.Paint(g, x, y, w, h);
		else
			H = 0;
	}

	public void OnTap() {
		map.ShowGeo();
	}
}

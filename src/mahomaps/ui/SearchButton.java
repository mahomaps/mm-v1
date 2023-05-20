package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;

public class SearchButton extends ControlButton {

	public SearchButton(IButtonHandler handler) {
		super(0, handler, 1);
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		if (MahoMapsApp.lastSearch == null)
			super.Paint(g, x, y, w, h);
	}
}

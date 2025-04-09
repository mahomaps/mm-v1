/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;

public class MenuButton extends ControlButton {

	public MenuButton(IButtonHandler handler) {
		super(1, handler, 2);
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		if (MahoMapsApp.route == null)
			super.Paint(g, x, y, w, h);
		else
			H = 0;
	}
}

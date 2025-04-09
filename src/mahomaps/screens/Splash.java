/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import tube42.lib.imagelib.ColorUtils;

public class Splash extends Canvas {

	private Image image;

	public Splash() {
		this.setFullScreenMode(true);

		try {
			image = Image.createImage("/splash.png");
		} catch (IOException e) {
		}
	}

	protected void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();

		final int c1 = 0xa74134;
		final int c2 = 0xf8503c;

		for (int i = 0; i < h; i++) {
			g.setColor(ColorUtils.blend(c1, c2, i * 255 / h));
			g.drawLine(0, i, w, i);
		}

		g.setColor(-1);

		if (this.image == null) {
			g.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE));
			int x = w >> 1;
			int y = h >> 1;
			g.drawString("Maho", x, y, Graphics.BOTTOM | Graphics.HCENTER);
			g.drawString("Maps", x, y, Graphics.TOP | Graphics.HCENTER);
		} else {
			g.drawImage(image, w >> 1, h >> 1, Graphics.VCENTER | Graphics.HCENTER);
		}

		if (MahoMapsApp.commit != null) {
			g.setFont(Font.getDefaultFont());
			g.drawString(MahoMapsApp.commit, w >> 1, h, Graphics.BOTTOM | Graphics.HCENTER);
		}
	}
}

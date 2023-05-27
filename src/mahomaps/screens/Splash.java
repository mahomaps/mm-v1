package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Splash extends Canvas {

	private Image image;

	public Splash() {
		this.setFullScreenMode(true);

		try {
			if (getWidth() < 256 || getHeight() < 240) {
				// 320x240 (horizontal 9.3) must show full image, so "H<240"
				image = Image.createImage("/icon.png");
			} else {
				image = Image.createImage("/splash.png");
			}
		} catch (IOException e) {
		}
	}

	protected void paint(Graphics g) {
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (this.image == null) {
			g.setColor(0xff0000);
			g.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE));
			int x = getWidth() >> 1;
			int y = getHeight() >> 1;
			g.drawString("Maho", x, y, Graphics.BOTTOM | Graphics.HCENTER);
			g.drawString("Maps", x, y, Graphics.TOP | Graphics.HCENTER);
		} else {
			g.drawImage(image, getWidth() >> 1, getHeight() >> 1, Graphics.VCENTER | Graphics.HCENTER);
		}
	}
}

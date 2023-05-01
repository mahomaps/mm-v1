package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Splash extends Canvas {

	private Image image;

	public Splash() {
		this.setFullScreenMode(true);

		try {
			this.image = Image.createImage("/splash.png");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	protected void paint(Graphics g) {
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (this.image != null) {
			g.drawImage(image, getWidth() >> 1, getHeight() >> 1, Graphics.VCENTER | Graphics.HCENTER);
		}
	}
}

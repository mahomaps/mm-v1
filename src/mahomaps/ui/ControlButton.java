package mahomaps.ui;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ControlButton extends UIElement implements ITouchAcceptor {

	private static Image sheet;
	protected int n;
	private final int size;
	private final IButtonHandler handler;
	private final int uid;
	private boolean hold;
	private int margin = 5;

	static {
		try {
			sheet = Image.createImage("/ui50.png");
		} catch (IOException e) {
			sheet = Image.createImage(1, 1);
			e.printStackTrace();
		}
	}

	public ControlButton(int n, IButtonHandler handler, int uid) {
		this.n = n;
		this.handler = handler;
		this.uid = uid;
		size = sheet.getWidth() >> 1;
		W = size + margin;
		H = size + margin;
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		g.drawRegion(sheet, hold ? size : 0, n * size, size, size, 0, x, y, 0);
		RegisterForInput(this, x, y, size, size);
	}

	public void OnPress() {
		hold = true;
	}

	public void OnRelease() {
		hold = false;
	}

	public void OnTap() {
		if (handler != null)
			handler.OnButtonTap(this, uid);
	}

}

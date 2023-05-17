package mahomaps.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ControlButton extends UIElement implements ITouchAcceptor {

	private final Image sheet;
	private final int n;
	private final int size;
	private final IButtonHandler handler;
	private final int uid;
	private boolean hold;

	public ControlButton(Image sheet, int n, IButtonHandler handler, int uid) {
		this.sheet = sheet;
		this.n = n;
		this.handler = handler;
		this.uid = uid;
		size = sheet.getWidth() >> 1;
		W = size;
		H = size;
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		g.drawRegion(sheet, hold ? size : 0, n * size, size, size, 0, x, y, 0);
	}

	public void OnPress() {
		hold = true;
	}

	public void OnRelease() {
		hold = false;
	}

	public void OnTap() {
		handler.OnButtonTap(this, uid);
	}

}

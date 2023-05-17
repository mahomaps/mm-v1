package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

public class MapOverlay extends UIElement implements ITouchAcceptor {
	public UIComposite content;

	public void Paint(Graphics g, int x, int y, int w, int h) {
		RegisterForInput(this, X, Y, W, H + 20);
		if (content == null)
			return;
		g.setColor(-1);
		g.fillRoundRect(X, Y, W, H + 20, 10, 10);
		content.Paint(g, X + 10, Y + 10, W - 20, 0);
		H = content.H;
	}

	public void OnPress() {
	}

	public void OnRelease() {
	}

	public void OnTap() {
	}

}

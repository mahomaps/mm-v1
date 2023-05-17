package mahomaps.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Button extends UIElement implements ITouchAcceptor {

	private String text;
	private int id;
	private IButtonHandler handler;
	private int margin;

	public Button(String text, int id, IButtonHandler handler, int margin) {
		this.text = text;
		this.id = id;
		this.handler = handler;
		this.margin = margin;
		this.H = Font.getFont(0, 0, 8).getHeight() + margin * 4;
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		RegisterForInput(this, x, y, w, h);
		g.setColor(0);
		g.setFont(Font.getFont(0, 0, 8));
		g.fillRoundRect(x + margin, y + margin, w - margin - margin, h - margin - margin, 10, 10);
		int fh = g.getFont().getHeight();
		g.setColor(-1);
		g.drawString(text, x + w / 2, y + h / 2 - fh / 2, Graphics.TOP | Graphics.HCENTER);
	}

	public void OnPress() {
		// TODO Auto-generated method stub

	}

	public void OnRelease() {
		// TODO Auto-generated method stub

	}

	public void OnTap() {
		if (handler != null)
			handler.OnButtonTap(this, id);
	}

}

package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

public class Button extends UIElement {

	private String text;
	private int id;
	private IButtonHandler handler;
	private int margin;

	public Button(String text, int id, IButtonHandler handler, int margin) {
		this.text = text;
		this.id = id;
		this.handler = handler;
		this.margin = margin;
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		g.setColor(0);
		g.fillRoundRect(x + margin, y + margin, w - margin - margin, h - margin - margin, 10, 10);
		int fh = g.getFont().getHeight();
		g.setColor(-1);
		g.drawString(text, x + w / 2, y + h / 2 - fh / 2, Graphics.TOP | Graphics.HCENTER);
	}

}

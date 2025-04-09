/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Button extends UIElement implements ITouchAcceptor {

	public String text;
	private int id;
	private IButtonHandler handler;
	public int margin = 3;
	private boolean hold = false;

	public Button(String text, int id, IButtonHandler handler) {
		this.text = text;
		this.id = id;
		this.handler = handler;
		UpdateHeight();
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		RegisterForInput(this, x, y, w, h);
		UpdateHeight();
		g.setFont(Font.getFont(0, 0, 8));
		if (hold) {
			g.setColor(0xFC6155);
			g.fillRoundRect(x + margin, y + margin, w - margin - margin, h - margin - margin, 10, 10);
			g.setColor(0x343434);
			if (UIElement.touchInput)
				g.fillRoundRect(x + margin * 2, y + margin * 2, w - margin * 4, h - margin * 4, 10, 10);
			else
				g.fillRoundRect(x + margin * 4, y + margin + 1, w - margin * 8, h - margin * 2 - 2, 10, 10);
		} else {
			g.setColor(0x343434);
			g.fillRoundRect(x + margin, y + margin, w - margin - margin, h - margin - margin, 10, 10);
		}
		int fh = g.getFont().getHeight();
		g.setColor(-1);
		g.drawString(text, x + w / 2, y + h / 2 - fh / 2, Graphics.TOP | Graphics.HCENTER);
	}

	private final void UpdateHeight() {
		this.H = Font.getFont(0, 0, 8).getHeight() + margin * (UIElement.touchInput ? 4 : 2);
	}

	public void OnPress() {
		hold = true;
	}

	public void OnRelease() {
		hold = false;
	}

	public void OnTap() {
		if (handler != null)
			handler.OnButtonTap(this, id);
	}

}

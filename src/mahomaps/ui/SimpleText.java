/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class SimpleText extends UIElement {

	private String text;
	private Font font;
	private int hAnchor;
	private int vAnchor;

	public SimpleText(String s, Font f, int hAnchor, int vAnchor) {
		this.text = s;
		if (f == null)
			f = Font.getFont(0, 0, 8);
		this.font = f;
		this.hAnchor = hAnchor;
		this.vAnchor = vAnchor;
		this.W = font.stringWidth(text);
		this.H = font.getHeight();
	}

	public SimpleText(String s) {
		this.text = s;
		this.font = Font.getFont(0, 0, 8);
		this.hAnchor = 0;
		this.vAnchor = 0;
		this.W = font.stringWidth(text);
		this.H = font.getHeight();
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		if (text == null)
			return;
		int rx;
		switch (hAnchor) {
		case 0:
		case Graphics.LEFT:
			hAnchor = Graphics.LEFT;
			rx = x + 3;
			break;
		case Graphics.HCENTER:
			rx = x + (w >> 1);
			break;
		case Graphics.RIGHT:
			rx = x + w - 3;
			break;
		default:
			throw new IllegalArgumentException();
		}
		int ry;
		int va;
		switch (vAnchor) {
		case 0:
		case Graphics.TOP:
			ry = y;
			va = Graphics.TOP;
			break;
		case Graphics.VCENTER:
			ry = y + (h >> 1) - (font.getHeight() >> 1);
			va = Graphics.TOP;
			break;
		case Graphics.BOTTOM:
			ry = y + h;
			va = Graphics.BOTTOM;
		default:
			throw new IllegalArgumentException();
		}

		g.setFont(font);
		g.setColor(-1);
		g.drawString(text, rx, ry, hAnchor | va);
	}

}

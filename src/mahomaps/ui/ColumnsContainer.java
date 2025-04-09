/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

public class ColumnsContainer extends UIComposite {

	public int stretchableElement = -1;

	public ColumnsContainer() {
	}

	public ColumnsContainer(UIElement[] elems) {
		super(elems);
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		int[] widths = new int[children.size()];
		int wsum = 0;
		int rh = 1;
		for (int i = 0; i < children.size(); i++) {
			UIElement el = ((UIElement) children.elementAt(i));
			widths[i] = el.W;
			wsum += el.W;
			if (el.H > rh)
				rh = el.H;
		}
		this.W = wsum;
		this.H = rh;
		if (stretchableElement > -1) {
			int avail = w - wsum;
			widths[stretchableElement] += avail;
		} else {
			int avail = (w - wsum) / widths.length;
			for (int i = 0; i < widths.length; i++) {
				widths[i] += avail;
			}
		}
		int rx = x;
		for (int i = 0; i < children.size(); i++) {
			UIElement el = ((UIElement) children.elementAt(i));
			el.Paint(g, rx, y, widths[i], rh);
			rx += widths[i];
		}
	}

}

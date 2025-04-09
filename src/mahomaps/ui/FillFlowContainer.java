/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import java.util.Enumeration;

import javax.microedition.lcdui.Graphics;

public class FillFlowContainer extends UIComposite {

	public FillFlowContainer() {
		super();
	}

	public FillFlowContainer(UIElement[] elems) {
		super(elems);
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		Enumeration e = children.elements();
		int th = 0;
		int mw = 0;
		while (e.hasMoreElements()) {
			UIElement elem = (UIElement) e.nextElement();
			elem.Paint(g, x, y, w, elem.H);
			y += elem.H;
			th += elem.H;
			if (elem.W > mw)
				mw = elem.W;
		}
		W = mw;
		H = th;
	}

}

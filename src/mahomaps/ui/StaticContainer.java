/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import java.util.Enumeration;

import javax.microedition.lcdui.Graphics;

public class StaticContainer extends UIComposite {

	public StaticContainer() {
		super();
	}

	public StaticContainer(UIElement[] elems) {
		super(elems);
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		Enumeration e = children.elements();
		while (e.hasMoreElements()) {
			UIElement elem = (UIElement) e.nextElement();
			elem.Paint(g, x + elem.X, y + elem.Y, elem.W, elem.H);
		}
	}

}

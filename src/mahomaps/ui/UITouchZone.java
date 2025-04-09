/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.ui;

import mahomaps.map.Rect;

public class UITouchZone extends Rect {

	public UITouchZone(ITouchAcceptor elem, int x, int y, int w, int h) {
		super(x, y, w, h);
		element = elem;
	}

	public final ITouchAcceptor element;

}

/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.map;

public class Rect {
	public int x, y, w, h;

	public Rect(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean contains(int px, int py) {
		return px > x && px < x + w && py > y && py < y + h;
	}

	public boolean containsBoth(int px1, int py1, int px2, int py2) {
		return contains(px1, py1) && contains(px2, py2);
	}
}

package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

public abstract class UIElement {

	public abstract void Paint(Graphics g, int x, int y, int w, int h);

	public int X, Y, W, H;
}

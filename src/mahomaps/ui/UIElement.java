package mahomaps.ui;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

public abstract class UIElement {

	public abstract void Paint(Graphics g, int x, int y, int w, int h);

	public int X, Y, W, H;

	// INPUT

	private static volatile Vector queue = new Vector();
	private static volatile Vector queueTemp = new Vector();

	private static UITouchZone holdElement = null;

	protected static synchronized void RegisterForInput(ITouchAcceptor ta, int x, int y, int w, int h) {
		queueTemp.addElement(new UITouchZone(ta, x, y, w, h));
	}

	public synchronized static void CommitInputQueue() {
		queue = queueTemp;
		queueTemp = new Vector(queue.size());
	}

	public static synchronized boolean InvokePressEvent(int x, int y) {
		Vector v = queue;
		for (int i = v.size() - 1; i >= 0; i--) {
			UITouchZone z = (UITouchZone) v.elementAt(i);
			if (z.contains(x, y)) {
				z.element.OnPress();
				holdElement = z;
				return true;
			}
		}
		return false;
	}

	public static synchronized void InvokeReleaseEvent() {
		UITouchZone z = holdElement;
		holdElement = null;
		if (z != null)
			z.element.OnRelease();
	}

	/**
	 * Looks through the input queue, and invokes touch event on last found
	 * acceptor.
	 *
	 * @param x X.
	 * @param y Y.
	 * @return false, if nothing was triggered.
	 */
	public static synchronized boolean InvokeTouchEvent(int x, int y) {
		Vector v = queue;
		for (int i = v.size() - 1; i >= 0; i--) {
			UITouchZone z = (UITouchZone) v.elementAt(i);
			if (z.contains(x, y)) {
				z.element.OnTap();
				return true;
			}
		}
		return false;
	}
}

package mahomaps.ui;

import mahomaps.MahoMapsApp;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

public abstract class UIElement {

	public abstract void Paint(Graphics g, int x, int y, int w, int h);

	public int X, Y, W, H;

	// INPUT

	private static volatile Vector queue = new Vector();
	private static volatile Vector queueTemp = new Vector();
	private static int selectedIndex = -1;
	private static boolean isSelectedInQueue = false;
	private static ITouchAcceptor holdElement = null;
	public static boolean touchInput;

	protected static synchronized void RegisterForInput(ITouchAcceptor ta, int x, int y, int w, int h) {
		queueTemp.addElement(new UITouchZone(ta, x, y, w, h));
		if (ta == holdElement)
			isSelectedInQueue = true;
	}

	public static synchronized void CommitInputQueue() {
		queue = queueTemp;
		queueTemp = new Vector(queue.size());
		if (holdElement != null) {
			if (!isSelectedInQueue) {
				InvokeReleaseEvent();
				selectedIndex = -1;
			}
		}
		isSelectedInQueue = false;
	}

	public static synchronized boolean InvokePressEvent(int x, int y) {
		InvokeReleaseEvent();
		Vector v = queue;
		for (int i = v.size() - 1; i >= 0; i--) {
			UITouchZone z = (UITouchZone) v.elementAt(i);
			if (z.contains(x, y)) {
				selectedIndex = i;
				z.element.OnPress();
				holdElement = z.element;
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean InvokePressEvent(ITouchAcceptor ta) {
		InvokeReleaseEvent();
		Vector v = queue;
		for (int i = v.size() - 1; i >= 0; i--) {
			UITouchZone z = (UITouchZone) v.elementAt(i);
			if (z.element == ta) {
				selectedIndex = i;
				z.element.OnPress();
				holdElement = z.element;
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean InvokePressEvent(int i) {
		InvokeReleaseEvent();
		if (i < 0)
			return false;
		if (i >= queue.size())
			return false;
		UITouchZone z = (UITouchZone) queue.elementAt(i);
		selectedIndex = i;
		z.element.OnPress();
		holdElement = z.element;
		return true;

	}

	public static synchronized void InvokeReleaseEvent() {
		ITouchAcceptor z = holdElement;
		holdElement = null;
		if (z != null)
			z.OnRelease();
	}

	public static synchronized void SelectUp() {
		selectedIndex--;
		if (selectedIndex < 0)
			selectedIndex = queue.size() - 1;
		InvokePressEvent(selectedIndex);
	}

	public static synchronized void SelectDown() {
		selectedIndex++;
		if (selectedIndex >= queue.size())
			selectedIndex = 0;
		InvokePressEvent(selectedIndex);
	}

	public static synchronized void Deselect() {
		selectedIndex = -1;
		InvokeReleaseEvent();
	}

	public static synchronized void TriggerSelected() {
		if (holdElement != null)
			holdElement.OnTap();
	}

	public static synchronized boolean IsQueueEmpty() {
		return queue.size() == 0;
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

	public static void SetTouchInput(boolean state) {
		boolean match = state == touchInput;
		touchInput = state;
		if (!match) {
			MahoMapsApp.Overlays().InvalidateAllOverlaysHeight();
		}
	}
}

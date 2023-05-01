package mahomaps.screens;

import javax.microedition.lcdui.game.GameCanvas;

public abstract class MultitouchCanvas extends GameCanvas {

	private final boolean isKemulator;

	protected MultitouchCanvas() {
		super(false);
		boolean ik;
		try {
			Class.forName("emulator.Emulator");
			ik = true;
		} catch (Exception e) {
			ik = false;
		}
		isKemulator = ik;
	}

	protected final void pointerDragged(int x, int y) {
		String pn = isKemulator ? null : System.getProperty("com.nokia.pointer.number");
		int n = pn == null ? 0 : (pn.charAt(0) - '0');
		pointerDragged(x, y, n);
	}

	protected final void pointerPressed(int x, int y) {
		String pn = isKemulator ? null : System.getProperty("com.nokia.pointer.number");
		int n = pn == null ? 0 : (pn.charAt(0) - '0');
		pointerPressed(x, y, n);
	}

	protected final void pointerReleased(int x, int y) {
		String pn = isKemulator ? null: System.getProperty("com.nokia.pointer.number");
		int n = pn == null ? 0 : (pn.charAt(0) - '0');
		pointerReleased(x, y, n);
	}

	protected abstract void pointerDragged(int x, int y, int n);

	protected abstract void pointerPressed(int x, int y, int n);

	protected abstract void pointerReleased(int x, int y, int n);
}

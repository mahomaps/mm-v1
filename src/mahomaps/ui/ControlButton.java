package mahomaps.ui;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ControlButton extends UIElement implements ITouchAcceptor {

	/**
	 * Do not access this directly! Use {@link #GetUiSheet()}.
	 */
	private static Image _sheet;
	protected int n;
	private final IButtonHandler handler;
	private final int uid;
	private boolean hold;
	private int margin = 5;

	protected static Image GetUiSheet() {
		if (_sheet != null)
			return _sheet;
		try {
			_sheet = Image.createImage("/ui50.png");
		} catch (IOException e) {
			_sheet = Image.createImage(1, 1);
			e.printStackTrace();
		}

		return _sheet;
	}

	public ControlButton(int n, IButtonHandler handler, int uid) {
		this.n = n;
		this.handler = handler;
		this.uid = uid;

	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		Image s = GetUiSheet();
		final int size = s.getWidth() >> 1;
		W = size + margin;
		H = size + margin;
		g.drawRegion(s, hold ? size : 0, n * size, size, size, 0, x, y, 0);
		RegisterForInput(this, x, y, size, size);
	}

	public void OnPress() {
		hold = true;
	}

	public void OnRelease() {
		hold = false;
	}

	public void OnTap() {
		if (handler != null)
			handler.OnButtonTap(this, uid);
	}

}

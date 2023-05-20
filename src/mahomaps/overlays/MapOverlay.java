package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.ui.ITouchAcceptor;
import mahomaps.ui.UIComposite;
import mahomaps.ui.UIElement;

public abstract class MapOverlay extends UIElement implements ITouchAcceptor {

	protected final static Vector EMPTY_VECTOR = new Vector();

	public abstract String GetId();

	public abstract Vector GetPoints();

	/**
	 * Обрабатывает нажатие на точку оверлея.
	 *
	 * @param p Точка.
	 * @return False, если нажатие нужно проигнорировать.
	 */
	public abstract boolean OnPointTap(Geopoint p);

	public UIComposite content;

	public void Paint(Graphics g, int x, int y, int w, int h) {
		RegisterForInput(this, x, y, w, H + 10);
		if (content == null)
			return;
		g.setColor(-1);
		g.fillRoundRect(x, y, w, H + 10, 10, 10);
		content.Paint(g, x + 5, y + 5, w - 10, 0);
		H = content.H;
	}

	protected void Close() {
		MahoMapsApp.GetCanvas().CloseOverlay(this);
	}

	public void OnPress() {
	}

	public void OnRelease() {
	}

	public void OnTap() {
	}

}

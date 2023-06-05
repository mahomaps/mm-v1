package mahomaps.overlays;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.ui.UIComposite;
import mahomaps.ui.UIElement;

public abstract class MapOverlay extends UIElement {

	public static final int OVERLAY_BG = 0x1E1E1E;
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
		if (content == null)
			return;
		g.setColor(OVERLAY_BG);
		g.fillRoundRect(x, y, w, H + 6, 10, 10);
		content.Paint(g, x + 3, y + 3, w - 6, 0);
		H = content.H;
	}

	protected void Close() {
		MahoMapsApp.Overlays().CloseOverlay(this);
	}

	protected void InvalidateSize() {
		MahoMapsApp.Overlays().InvalidateOverlayHeight(this);
	}

}

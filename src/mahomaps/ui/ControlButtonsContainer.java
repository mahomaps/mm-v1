package mahomaps.ui;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.screens.MapCanvas;

public class ControlButtonsContainer extends UIElement implements IButtonHandler {

	private final MapCanvas map;
	private final UIElement[] btns;
	public Vector info;

	public ControlButtonsContainer(MapCanvas map) {
		this.map = map;
		btns = new UIElement[] { new SearchButton(this), new ControlButton(1, this, 2), new ControlButton(2, this, 3),
				new ControlButton(3, this, 4), new GeolocationButton(map) };
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		int cy = h;
		int cx = w;
		for (int i = 4; i >= 0; i--) {
			UIElement elem = btns[i];
			cy -= elem.H;
			if (cy < 0) {
				cy = h - elem.H;
				cx -= elem.W;
				if (cy < 0)
					return;
			}
			elem.Paint(g, cx - elem.W, cy, elem.W, elem.H);
		}
	}

	public void PaintInfo(Graphics g, int x, int y, int w, int h) {
		Vector s = info;
		if (s != null && s.size() > 0) {
			Font f = Font.getFont(0, 0, 8);
			final int fh = f.getHeight();
			final int ih = s.size() * fh;
			int iy = h - ih - 3 - 5;
			int iw = 0;
			for (int i = 0; i < s.size(); i++) {
				int lw = f.stringWidth((String) s.elementAt(i)) + 6;
				if (lw > iw)
					iw = lw;
			}
			g.setFont(f);
			g.setColor(0x1E1E1E);
			g.fillRoundRect(5, iy - 3, iw + 6, ih + 6, 10, 10);
			g.setColor(-1);
			for (int i = 0; i < s.size(); i++) {
				g.drawString((String) s.elementAt(i), 11, iy, 0);
				iy += fh;
			}
		}
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case 1:
			map.BeginTextSearch();
			break;
		case 2:
			MahoMapsApp.BringMenu();
			break;
		case 3:
			map.state = map.state.ZoomIn();
			break;
		case 4:
			map.state = map.state.ZoomOut();
			break;
		}
	}
}

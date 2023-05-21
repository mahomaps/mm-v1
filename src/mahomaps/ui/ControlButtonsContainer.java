package mahomaps.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.screens.MapCanvas;

public class ControlButtonsContainer extends UIElement implements IButtonHandler {

	private final FillFlowContainer flow;
	private final MapCanvas map;

	public String[] info;

	public ControlButtonsContainer(MapCanvas map) {
		this.map = map;
		this.flow = new FillFlowContainer(new UIElement[] { new SearchButton(this), new ControlButton(1, this, 2),
				new ControlButton(2, this, 3), new ControlButton(3, this, 4), new GeolocationButton(map) });
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		flow.Paint(g, w - flow.W, h - flow.H, flow.W, flow.H);

		String[] s = info;
		if (s != null) {
			Font f = Font.getFont(0, 0, 8);
			final int fh = f.getHeight();
			final int ih = s.length * fh;
			int iy = ih - 3 - 5;
			int iw = 0;
			for (int i = 0; i < s.length; i++) {
				int lw = f.stringWidth(s[i]);
				if (lw > iw)
					iw = lw;
			}
			g.setFont(f);
			g.setColor(0x1E1E1E);
			g.fillRoundRect(5, iy - 3, iw + 6, ih + 6, 10, 10);
			g.setColor(-1);
			for (int i = 0; i < s.length; i++) {
				g.drawString(s[i], 8, iy, 0);
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

package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.MahoMapsApp;
import mahomaps.screens.MapCanvas;

public class ControlButtonsContainer extends UIElement implements IButtonHandler {

	private final FillFlowContainer flow;
	private final MapCanvas map;

	public ControlButtonsContainer(MapCanvas map) {
		this.map = map;
		this.flow = new FillFlowContainer(new UIElement[] { new SearchButton(this), new ControlButton(1, this, 2),
				new ControlButton(2, this, 3), new ControlButton(3, this, 4), new GeolocationButton(map) });
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		flow.Paint(g, w - flow.W, h - flow.H, flow.W, flow.H);
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

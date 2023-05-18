package mahomaps.ui;

import javax.microedition.lcdui.Graphics;

import mahomaps.screens.MapCanvas;

public class ControlButtonsContainer extends UIElement {

	private final FillFlowContainer flow;

	public ControlButtonsContainer(MapCanvas map) {
		this.flow = new FillFlowContainer(new UIElement[] { new ControlButton(0, map, 1), new ControlButton(1, map, 2),
				new ControlButton(2, map, 3), new ControlButton(3, map, 4), new GeolocationButton(map) });
	}

	public void Paint(Graphics g, int x, int y, int w, int h) {
		flow.Paint(g, w - flow.W, h - flow.H, flow.W, flow.H);
	}

}

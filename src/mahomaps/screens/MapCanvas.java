package mahomaps.screens;

import javax.microedition.lcdui.game.GameCanvas;

import mahomaps.map.TilesProvider;

public class MapCanvas extends GameCanvas {

	private TilesProvider tiles;

	public MapCanvas(TilesProvider tiles) {
		super(false);
		this.tiles = tiles;
	}

}

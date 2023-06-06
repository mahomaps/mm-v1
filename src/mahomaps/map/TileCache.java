package mahomaps.map;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;

public class TileCache extends TileId {

	public Image img;
	public volatile int state = 0;
	public volatile int unuseCount;

	public TileCache(int x, int y, int zoom) {
		super(x, y, zoom);
	}

	public TileCache(TileId id) {
		super(id.x, id.y, id.zoom);
	}

	public boolean is(TileId id) {
		return id.x == x && id.y == y && id.zoom == zoom;
	}

	public void paint(Graphics g, int tx, int ty) {
		Font f = Font.getFont(0, 0, 8);
		int vo = f.getHeight();
		g.setFont(f);
		if (state == STATE_READY) {
			g.drawImage(img, tx, ty, 0);
			g.setColor(0, 0, 255);
		} else {
			g.setGrayScale(0);
			g.drawString(GetState(), tx + 128, ty + 128 - vo, Graphics.TOP | Graphics.HCENTER);
		}
		if (Settings.drawDebugInfo) {
			g.drawRect(tx, ty, 255, 255);
			g.fillRect(tx + 125, ty + 63, 6, 2);
			g.fillRect(tx + 127, ty + 61, 2, 6);
			g.fillRect(tx + 125, ty + 127, 6, 2);
			g.fillRect(tx + 127, ty + 125, 2, 6);
			g.fillRect(tx + 125, ty + 191, 6, 2);
			g.fillRect(tx + 127, ty + 189, 2, 6);
			g.drawString("x=" + this.x + " y=" + this.y, tx + 1, ty + 1, 0);
		}
	}

	public String GetState() {
		if (state < 0)
			return "";
		if (state > STATE_MISSING)
			return "";
		return STATE_DESCRIPTION[state];
	}

	public static final String[] STATE_DESCRIPTION = new String[] { MahoMapsApp.text[97], MahoMapsApp.text[98],
			MahoMapsApp.text[99], MahoMapsApp.text[100], MahoMapsApp.text[94], MahoMapsApp.text[88],
			MahoMapsApp.text[101], MahoMapsApp.text[102] };

	public static final int STATE_CACHE_PENDING = 0;
	public static final int STATE_CACHE_LOADING = 1;
	public static final int STATE_SERVER_PENDING = 2;
	public static final int STATE_SERVER_LOADING = 3;
	public static final int STATE_READY = 4;
	public static final int STATE_ERROR = 5;
	public static final int STATE_UNLOADED = 6;
	public static final int STATE_MISSING = 7;
}

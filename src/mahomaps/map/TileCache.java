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

	public TileCache(int x, int y, int zoom, int map) {
		super(x, y, zoom, map);
	}

	public TileCache(TileId id) {
		super(id.x, id.y, id.zoom, id.map);
	}

	public boolean is(TileId id) {
		return id.x == x && id.y == y && id.zoom == zoom && id.map == map;
	}

	public void paint(Graphics g, int tx, int ty) {
		Font f = Font.getFont(0, 0, 8);
		int vo = f.getHeight();
		g.setFont(f);
		Image i = img;
		if (i != null) {
			g.drawImage(i, tx, ty, 0);
		}
		if (state == STATE_READY) {
			if (i == null) // wtf!?
				throw new NullPointerException("Corrupted tile state!");
			g.setColor(0, 0, 255);
		} else {
			int ax = tx + 128;
			int ay = ty + 128 - (vo / 2);

			g.setGrayScale(255);
			g.drawString(GetState(), ax + 1, ay + 1, Graphics.TOP | Graphics.HCENTER);
			g.drawString(GetState(), ax - 1, ay + 1, Graphics.TOP | Graphics.HCENTER);
			g.drawString(GetState(), ax + 1, ay - 1, Graphics.TOP | Graphics.HCENTER);
			g.drawString(GetState(), ax - 1, ay - 1, Graphics.TOP | Graphics.HCENTER);

			g.setGrayScale(0);
			g.drawString(GetState(), ax, ay, Graphics.TOP | Graphics.HCENTER);
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
		return MahoMapsApp.text[STATE_DESCRIPTION[state]];
	}

	public static final int[] STATE_DESCRIPTION = new int[] { 97, 98, 99, 100, 94, 88, 101, 102 };

	public static final int STATE_CACHE_PENDING = 0;
	public static final int STATE_CACHE_LOADING = 1;
	public static final int STATE_SERVER_PENDING = 2;
	public static final int STATE_SERVER_LOADING = 3;
	public static final int STATE_READY = 4;
	public static final int STATE_ERROR = 5;
	public static final int STATE_UNLOADED = 6;
	public static final int STATE_MISSING = 7;
}

package mahomaps.map;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONObject;
import mahomaps.Settings;

public class MapState {
	public int tileX, tileY;
	public int xOffset, yOffset;
	public int zoom;

	public final void ClampOffset() {
		while (xOffset > 0) {
			tileX--;
			xOffset -= 256;
		}
		while (yOffset > 0) {
			tileY--;
			yOffset -= 256;
		}
		while (xOffset < -255) {
			tileX++;
			xOffset += 256;
		}
		while (yOffset < -255) {
			tileY++;
			yOffset += 256;
		}

		final int tc = (1 << zoom);

		if (tileX < 0)
			tileX += tc;
		if (tileX >= tc)
			tileX -= tc;
		if (tileY < 0) {
			tileY = 0;
			yOffset = 0;
		}
		if (tileY >= tc) {
			tileY = tc - 1;
			yOffset = -255;
		}
	}

	public MapState Clone() {
		MapState ms = new MapState();
		ms.tileX = tileX;
		ms.tileY = tileY;
		ms.xOffset = xOffset;
		ms.yOffset = yOffset;
		ms.zoom = zoom;
		return ms;
	}

	public MapState ZoomIn() {
		if (zoom >= 18)
			return Clone();
		MapState ms = new MapState();
		ms.zoom = zoom + 1;
		ms.tileX = tileX * 2;
		ms.tileY = tileY * 2;
		ms.xOffset = xOffset * 2;
		ms.yOffset = yOffset * 2;
		ms.ClampOffset();
		return ms;
	}

	public MapState ZoomOut() {
		if (zoom <= 0)
			return Clone();
		MapState ms = new MapState();
		ms.zoom = zoom - 1;
		ms.tileX = tileX / 2;
		ms.tileY = tileY / 2;
		ms.xOffset = (xOffset - (tileX % 2 == 1 ? 256 : 0)) / 2;
		ms.yOffset = (yOffset - (tileY % 2 == 1 ? 256 : 0)) / 2;
		ms.ClampOffset();
		return ms;
	}

	public static MapState FocusAt(Geopoint p) {
		return FocusAt(p, Settings.focusZoom);
	}

	public static MapState FocusAt(Geopoint p, int zoom) {
		MapState ms = new MapState();
		ms.zoom = zoom;
		ms.xOffset -= p.GetScreenX(ms);
		ms.yOffset -= p.GetScreenY(ms);
		ms.ClampOffset();
		return ms;
	}

	public static MapState Default() {
		MapState ms = new MapState();
		ms.zoom = 0;
		ms.xOffset = -128;
		ms.yOffset = -128;
		return ms;
	}

	public String Encode() {
		JSONObject j = new JSONObject();
		j.put("z", zoom);
		j.put("x", tileX);
		j.put("y", tileY);
		j.put("xo", xOffset);
		j.put("yo", yOffset);
		return j.toString();
	}

	public static MapState Decode(String s) {
		final JSONObject j = JSON.getObject(s);
		final MapState ms = new MapState();
		ms.zoom = j.getInt("z", 0);
		ms.tileX = j.getInt("x", 0);
		ms.tileY = j.getInt("y", 0);
		ms.xOffset = j.getInt("xo", 0);
		ms.yOffset = j.getInt("yo", 0);
		ms.ClampOffset();
		return ms;
	}

	public String toString() {
		return "X: " + xOffset + "|" + tileX + " Y: " + yOffset + "|" + tileY + " Z: " + zoom;
	}
}

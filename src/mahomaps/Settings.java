package mahomaps;

import javax.microedition.rms.RecordStore;

import org.json.me.JSONObject;

public class Settings {

	private static final String RMS_NAME = "mm_v1_prefs";

	private Settings() {
	}

	public static boolean drawTileInfo = false;
	public static int focusZoom = 16;
	public static int geoLook = 0;
	public static int showGeo = 2;
	public static boolean allowDownload = true;
	public static int cacheMode = 1;
	public static boolean proxyTiles = false;

	public static final int CACHE_FS = 1;
	public static final int CACHE_RMS = 2;
	public static final int CACHE_DISABLED = 0;

	public static boolean Read() {
		try {
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);
			byte[] d = null;
			if (r.getNumRecords() > 0) {
				d = r.getRecord(1);
			}
			r.closeRecordStore();

			// parse
			if (d == null)
				return true;

			JSONObject j = new JSONObject(new String(d));
			drawTileInfo = j.optBoolean("tile_info", false);
			focusZoom = j.optInt("focus_zoom", 16);
			geoLook = j.optInt("geo_look", 0);
			showGeo = j.optInt("show_geo", 2);
			allowDownload = j.optBoolean("online", true);
			cacheMode = j.optInt("cache", 1);
			proxyTiles = j.optBoolean("proxy_tiles");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String Serialize() {
		JSONObject j = new JSONObject();
		j.put("tile_info", drawTileInfo);
		j.put("focus_zoom", focusZoom);
		j.put("geo_look", geoLook);
		j.put("show_geo", showGeo);
		j.put("online", allowDownload);
		j.put("cache", cacheMode);
		j.put("proxy_tiles", proxyTiles);
		return j.toString();
	}

	public static void Save() {
		try {
			byte[] d = Serialize().getBytes();
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);

			if (r.getNumRecords() == 0) {
				r.addRecord(new byte[1], 0, 1);
			}
			r.setRecord(1, d, 0, d.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

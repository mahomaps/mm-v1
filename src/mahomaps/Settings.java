package mahomaps;

import javax.microedition.rms.RecordStore;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONObject;

public class Settings {

	private static final String RMS_NAME = "mm_v1_prefs";

	private Settings() {
	}

	public static boolean drawDebugInfo = false;
	public static int focusZoom = 16;
	public static int geoLook = 0;
	public static int showGeo = 2;
	public static boolean allowDownload = true;
	public static int cacheMode = 1;
	public static boolean proxyTiles = false;
	public static boolean proxyApi = false;
	public static int uiSize = 0;
	public static int lang = 0;

	public static String proxyServer = "http://nnp.nnchan.ru:80/mahoproxy.php?u=";

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
			if (d == null) {
				ApplyOptimal();
				return true;
			}

			JSONObject j = JSON.getObject(new String(d));
			drawDebugInfo = j.getBoolean("tile_info", false);
			focusZoom = j.getInt("focus_zoom", 16);
			geoLook = j.getInt("geo_look", 0);
			showGeo = j.getInt("show_geo", 2);
			allowDownload = j.getBoolean("online", true);
			cacheMode = j.getInt("cache", 1);
			proxyTiles = j.getBoolean("proxy_tiles", false);
			proxyApi = j.getBoolean("proxy_api", false);
			uiSize = j.getInt("ui_size", 0);
			lang = j.getInt("lang", 0);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			ApplyOptimal();
			return false;
		}
	}

	private static void ApplyOptimal() {
		if (MahoMapsApp.IsKemulator()) {
			proxyApi = false;
			proxyTiles = false;
		} else {
			proxyTiles = proxyApi = MahoMapsApp.platform == null
					|| MahoMapsApp.platform.indexOf("platform_version=5.") == -1
					|| MahoMapsApp.platform.indexOf("platform_version=5.0") != -1;
		}
	}

	public static String Serialize() {
		JSONObject j = new JSONObject();
		j.put("tile_info", drawDebugInfo);
		j.put("focus_zoom", focusZoom);
		j.put("geo_look", geoLook);
		j.put("show_geo", showGeo);
		j.put("online", allowDownload);
		j.put("cache", cacheMode);
		j.put("proxy_tiles", proxyTiles);
		j.put("proxy_api", proxyApi);
		j.put("ui_size", uiSize);
		j.put("lang", lang);
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

	public static String GetLangString() {
		switch (lang) {
		case 0:
			return new String(new char[] {'r','u','_','R','U'});
		case 1:
			return new String(new char[] {'e','n','_','U','S'});
		case 2:
			return new String(new char[] {'t','r','_','T','R'});
		default:
			throw new IndexOutOfBoundsException("Unknown language code!");
		}
	}
}

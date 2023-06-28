package mahomaps;

import javax.microedition.rms.RecordStore;

import cc.nnproject.json.*;

import mahomaps.map.MapState;

public class Settings {

	private static final String RMS_NAME = "mm_v1_prefs";
	private static final String POS_RMS_NAME = "mm_v1_state";

	private Settings() {
	}

	public static boolean drawDebugInfo = false;
	public static int focusZoom = 16;
	public static int geoLook = 0;
	public static int showGeo = 1;
	public static boolean allowDownload = true;
	public static int cacheMode = 1;
	public static boolean proxyTiles = false;
	public static boolean proxyApi = false;
	public static int uiSize = 0;
	public static int apiLang = 0;
	public static int uiLang = 0;
	public static boolean bbWifi;
	public static boolean firstLaunch = true;
	public static int map;

	public static String proxyServer = "http://nnp.nnchan.ru:80/mahoproxy.php?u=";

	public static final int CACHE_FS = 1;
	public static final int CACHE_RMS = 2;
	public static final int CACHE_DISABLED = 0;

	/**
	 * Telemetry flags
	 * <ul>
	 * <li>1 - about screen
	 * <li>2 - others screen
	 * <li>4 - settings screen
	 * <li>8 - geolocation
	 * <li>16 - search
	 * <li>32 - route build by foot
	 * <li>64 - route build by auto
	 * <li>128 - route build by PT
	 * <li>256 - route follow
	 * <li>512 - route details
	 * </ul>
	 */
	public static int usageFlags = 0;

	public static synchronized void PushUsageFlag(int flag) {
		usageFlags |= flag;
		Save();
	}

	/**
	 * Читает настройки приложения. Вызывает загрузку локализации.
	 *
	 * @return False, если чтение неудачно.
	 */
	public static synchronized boolean Read() {
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
			showGeo = j.getInt("show_geo", 1);
			allowDownload = j.getBoolean("online", true);
			cacheMode = j.getInt("cache", 1);
			proxyTiles = j.getBoolean("proxy_tiles");
			proxyApi = j.getBoolean("proxy_api");
			proxyServer = j.getString("proxy_server", proxyServer);
			uiSize = j.getInt("ui_size", 0);
			apiLang = j.getInt("lang", 0);
			uiLang = j.getInt("ui_lang", 0);
			bbWifi = j.getBoolean("bb_wifi", false);
			firstLaunch = j.getBoolean("1", true);
			usageFlags = j.getInt("tm", 0);
			map = j.getInt("map", 0);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			ApplyOptimal();
			return false;
		} finally {
			try {
				MahoMapsApp.LoadLocale(GetUiLangFile());
			} catch (Exception e) {
				MahoMapsApp.LoadLocale(new String(new char[] { 'r', 'u' }));
			}
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
		j.put("proxy_server", proxyServer);
		j.put("ui_size", uiSize);
		j.put("lang", apiLang);
		j.put("ui_lang", uiLang);
		j.put("bb_wifi", bbWifi);
		j.put("1", firstLaunch);
		j.put("tm", usageFlags);
		j.put("map", map);
		return j.toString();
	}

	public static synchronized void Save() {
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

	public static MapState ReadStateOrDefault() {
		try {
			RecordStore r = RecordStore.openRecordStore(POS_RMS_NAME, true);
			byte[] d = null;
			if (r.getNumRecords() > 0) {
				d = r.getRecord(1);
			}
			r.closeRecordStore();

			if (d != null) {
				return MapState.Decode(new String(d));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return MapState.Default();
	}

	public static void SaveState(MapState ms) {
		try {
			byte[] d = ms.Encode().getBytes();
			RecordStore r = RecordStore.openRecordStore(POS_RMS_NAME, true);
			if (r.getNumRecords() == 0)
				r.addRecord(new byte[1], 0, 1);
			r.setRecord(1, d, 0, d.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets language string for use in API requests.
	 */
	public static String GetLangString() {
		switch (apiLang) {
		case 0:
			return new String(new char[] { 'r', 'u', '_', 'R', 'U' });
		case 1:
			return new String(new char[] { 'e', 'n', '_', 'U', 'S' });
		case 2:
			return new String(new char[] { 't', 'r', '_', 'T', 'R' });
		default:
			throw new IndexOutOfBoundsException("Unknown language code!");
		}
	}

	public static String GetUiLangFile() {
		switch (uiLang) {
		case 0:
		default:
			return new String(new char[] { 'r', 'u' });
		case 1:
			return new String(new char[] { 'e', 'n' });
		case 2:
			return new String(new char[] { 'f', 'r' });
		}
	}
}

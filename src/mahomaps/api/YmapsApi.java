/*
Copyright (c) 2023 Fyodor Ryzhov
Copyright (c) 2023-2024 Arman Jussupgaliyev
*/
package mahomaps.api;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.*;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;
import mahomaps.map.Geopoint;

public final class YmapsApi extends YmapsApiBase {

	private static final String RMS_NAME = "mm_v1_api";

	public static final String key = "d81964d6-b80c-46ed-9b29-d980a45d32f9";

	public String token = null;

	public final synchronized void RefreshToken() throws IOException {
		token = null;
		token = GetToken(key);
		Save();
	}

	public final synchronized void RefreshTokenIfNull() throws IOException {
		if (token == null) RefreshToken();
	}


	private final String GetSearchUrl(String text, Geopoint around, double zone) {
		String[] cs = around.GetRounded();
		return "https://api-maps.yandex.ru/services/search/v2/?format=json&lang=" + Settings.GetLangString() + "&token="
				+ token + "&rspn=0&results=40&origin=jsapi2SearchControl"
				+ "&snippets=businessrating%2F1.x%2Cmasstransit%2F1.x&ask_direct=1&experimental_maxadv=200&apikey="
				+ key + "&text=" + EncodeUrl(text) + "&ll=" + cs[1] + "%2C" + cs[0] + "&spn=" + zone + "%2C" + zone;
	}

	private final String GetVisibleVehiclesUrl(Geopoint around, double zone) {
		String[] cs = around.GetRounded();
		return "http://localhost:5249/ym/mt/vt?lang=ru&ll=" + cs[1] + "%2C" + cs[0] + "&spn=" + zone + "%2C" + zone;
	}

	private final String GetActiveThreadUrl(String tid, String lid, String vid) {
		return "http://localhost:5249/ym/mt/vt?lang=ru&tid=" + tid + "&lid=" + lid + "&vid=" + vid;
	}

	private final String GetRouteUrl(Geopoint a, Geopoint b, int type) {
		String typeS = "";
		switch (type) {
			case ROUTE_BYFOOT:
				typeS = "&rtt=pd";
				break;
			case ROUTE_AUTO:
				break;
			case ROUTE_TRANSPORT:
				typeS = "&rtt=mt";
				break;
			default:
				throw new IllegalArgumentException();
		}
		return "https://api-maps.yandex.ru/services/route/2.0/?lang=" + Settings.GetLangString() + "&token=" + token
				+ "&rll=" + a.lon + "%2C" + a.lat + "~" + b.lon + "%2C" + b.lat + "&rtm=dtr&results=1&apikey=" + key
				+ typeS;
	}

	public final JSONArray Search(String text, Geopoint around, double zone)
			throws JSONException, IOException {
		RefreshTokenIfNull();
		try {
			String response = GetUtf(GetSearchUrl(text, around, zone));
			JSONObject j = JSON.getObject(response);
			CheckAccessError(j);
			return j.getArray("features");
		} catch (AccessErrorException ex) {
			RefreshToken();
			return Search(text, around, zone);
		}
	}

	public final JSONArray Routes(Geopoint a, Geopoint b, int type)
			throws JSONException, IOException {
		RefreshTokenIfNull();
		try {
			String response = GetUtf(GetRouteUrl(a, b, type));
			JSONObject j = JSON.getObject(response);
			CheckAccessError(j);
			JSONArray ja = j.getArray("features");
			if (ja.size() == 0)
				throw new ConnectionNotFoundException();
			return ja.getObject(0).getArray("features");
		} catch (AccessErrorException ex) {
			RefreshToken();
			return Routes(a, b, type);
		}
	}

	public final JSONArray Vehicles(Geopoint around, double zone) {
		try {
			String response = GetUtf(GetVisibleVehiclesUrl(around, zone));
			return JSON.getArray(response);
		} catch (Exception ex) {
			MahoMapsApp.lastException = ex;
			return new JSONArray();
		}
	}

	public static final int ROUTE_BYFOOT = 1;
	public static final int ROUTE_AUTO = 2;
	public static final int ROUTE_TRANSPORT = 3;

	public void Save() {
		JSONObject j = new JSONObject();
		if (token != null)
			j.put("token", token);
		JSONObject obj = SaveCookies();
		if (obj != null && obj.size() != 0)
			j.put("cookies", SaveCookies());
		j.put("time", System.currentTimeMillis());

		try {
			byte[] d = j.toString().getBytes();
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);
			try {
				try {
					if (r.getNumRecords() != 0)
						r.deleteRecord(r.getNextRecordID() - 1);
				} catch (Exception ignored) {
				}
				r.addRecord(d, 0, d.length);
			} finally {
				r.closeRecordStore();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final synchronized void TryRead() {
		try {
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, false);
			byte[] d;
			try {
				d = r.getRecord(r.getNextRecordID() - 1);
			} finally {
				r.closeRecordStore();
			}

			// parse
			if (d == null) return;

			JSONObject j = JSON.getObject(new String(d));
			token = j.getNullableString("token");
			long dif = j.getLong("time", 0);
			// reset session each 8 hours (1000ms * 60s * 60m * 8h)
			if (dif > 1000L * 3600L * 8L)
				return;
			JSONObject cs = j.getNullableObject("cookies");
			if (cs != null && cs.size() != 0 && token != null)
				LoadCookies(cs);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

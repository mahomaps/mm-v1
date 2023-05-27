package mahomaps.api;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.rms.RecordStore;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import mahomaps.map.Geopoint;

public final class YmapsApi extends YmapsApiBase {

	private static final String RMS_NAME = "mm_v1_api";

	public final String key = "d81964d6-b80c-46ed-9b29-d980a45d32f9";

	public String token = null;

	public final synchronized void RefreshToken() throws Exception {
		token = null;
		token = GetToken(key);
		Save();
	}

	private final String GetSearchUrl(String text, Geopoint around, double zone) {
		String[] cs = around.GetRounded();
		return "https://api-maps.yandex.ru/services/search/v2/?format=json&lang=ru_RU&token=" + token
				+ "&rspn=0&results=40&origin=jsapi2SearchControl"
				+ "&snippets=businessrating%2F1.x%2Cmasstransit%2F1.x&ask_direct=1&experimental_maxadv=200&apikey="
				+ key + "&text=" + EncodeUrl(text) + "&ll=" + cs[1] + "%2C" + cs[0] + "&spn=" + zone + "%2C" + zone;
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
		return "https://api-maps.yandex.ru/services/route/2.0/?lang=ru_RU&token=" + token + "&rll=" + a.lon + "%2C"
				+ a.lat + "~" + b.lon + "%2C" + b.lat + "&rtm=dtr&results=1&apikey=" + key + typeS;
	}

	public final JSONArray Search(String text, Geopoint around, double zone)
			throws JSONException, IOException, Http403Exception {
		JSONArray j = (new JSONObject(GetUtf(GetSearchUrl(text, around, zone)))).getJSONArray("features");
		return j;
	}

	public final JSONObject Route(Geopoint a, Geopoint b, int type)
			throws JSONException, IOException, Http403Exception {
		JSONArray j = (new JSONObject(GetUtf(GetRouteUrl(a, b, type)))).getJSONArray("features");
		if (j.length() == 0)
			throw new ConnectionNotFoundException();
		JSONObject j1 = j.getJSONObject(0).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
		return j1;
	}

	public static final int ROUTE_BYFOOT = 1;
	public static final int ROUTE_AUTO = 2;
	public static final int ROUTE_TRANSPORT = 3;

	public void Save() {
		JSONObject j = new JSONObject();
		if (token != null)
			j.put("token", token);
		JSONObject obj = SaveCookies();
		if (obj != null && obj.length() != 0)
			j.put("cookies", SaveCookies());

		try {
			byte[] d = j.toString().getBytes();
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

	public final synchronized void TryRead() {
		try {
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);
			byte[] d = null;
			if (r.getNumRecords() > 0) {
				d = r.getRecord(1);
			}
			r.closeRecordStore();

			// parse
			if (d == null)
				return;

			JSONObject j = new JSONObject(new String(d));
			token = j.optString("token", null);
			JSONObject cs = j.optJSONObject("cookies");
			if (cs != null && cs.length() != 0 && token != null)
				LoadCookies(cs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

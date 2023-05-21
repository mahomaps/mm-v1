package mahomaps.api;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import mahomaps.map.Geopoint;

public final class YmapsApi extends YmapsApiBase {

	public final String key = "d81964d6-b80c-46ed-9b29-d980a45d32f9";

	public String token = null;

	public final synchronized void RefreshToken() throws Exception {
		token = null;
		token = GetToken(key);
	}

	private final String GetSearchUrl(String text, Geopoint around, double zone) {
		return "https://api-maps.yandex.ru/services/search/v2/?format=json&lang=ru_RU&token=" + token
				+ "&rspn=0&results=100&origin=jsapi2SearchControl"
				+ "&snippets=businessrating%2F1.x%2Cmasstransit%2F1.x&ask_direct=1&experimental_maxadv=200&apikey="
				+ key + "&text=" + EncodeUrl(text) + "&ll=" + around.lat + "%2C" + around.lon + "&spn=" + zone + "%2C"
				+ zone;
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

	public final JSONArray Search(String text, Geopoint around, double zone) throws JSONException, IOException {
		JSONArray j = (new JSONObject(GetUtf(GetSearchUrl(text, around, zone)))).getJSONArray("features");
		return j;
	}

	public final JSONObject Route(Geopoint a, Geopoint b, int type) throws JSONException, IOException {
		JSONArray j = (new JSONObject(GetUtf(GetRouteUrl(a, b, type)))).getJSONArray("features");
		if (j.length() == 0)
			throw new ConnectionNotFoundException();
		JSONObject j1 = j.getJSONObject(0).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
		return j1;
	}

	public static final int ROUTE_BYFOOT = 1;
	public static final int ROUTE_AUTO = 2;
	public static final int ROUTE_TRANSPORT = 3;
}

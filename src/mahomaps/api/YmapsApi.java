package mahomaps.api;

import java.io.IOException;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import mahomaps.map.Geopoint;

public final class YmapsApi extends YmapsApiBase {

	public final String key = "d81964d6-b80c-46ed-9b29-d980a45d32f9";

	public String token = null;

	public final synchronized boolean RefreshToken() {
		token = null;
		token = GetToken(key);
		return token != null;
	}

	private final String GetSearchUrl(String text, Geopoint around, double zone) {
		return "https://api-maps.yandex.ru/services/search/v2/?format=json&lang=ru_RU&token=" + token
				+ "&rspn=0&results=100&origin=jsapi2SearchControl"
				+ "&snippets=businessrating%2F1.x%2Cmasstransit%2F1.x&ask_direct=1&experimental_maxadv=200&apikey="
				+ key + "&text=" + EncodeUrl(text) + "&ll=" + around.lat + "%2C" + around.lon + "&spn=" + zone + "%2C"
				+ zone;
	}

	public final JSONArray Search(String text, Geopoint around, double zone) throws JSONException, IOException {
		JSONArray j = (new JSONObject(GetUtf(GetSearchUrl(text, around, zone)))).getJSONArray("features");
		return j;
	}

}

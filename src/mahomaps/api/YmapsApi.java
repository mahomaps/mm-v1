package mahomaps.api;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public final class YmapsApi {

	public final String key = "d81964d6-b80c-46ed-9b29-d980a45d32f9";
	private final String tokenMark = "token\":\"";

	public String token = null;

	public final String GetJsUrl() {
		return "https://api-maps.yandex.ru/2.1/?lang=ru_RU&apikey=" + key;
	}

	public final boolean RefreshToken() {
		HttpConnection hc = null;
		InputStream raw = null;
		InputStreamReader stream = null;
		try {
			hc = (HttpConnection) Connector.open(GetJsUrl());
			hc.setRequestMethod("GET");
			hc.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0");
			int r = hc.getResponseCode();
			raw = hc.openInputStream();
			stream = new InputStreamReader(raw, "UTF-8");
			while (true) {
				int c = stream.read();
				if (c == -1)
					throw new EOFException();
				if (c == '"') {
					boolean ok = true;
					for (int i = 0; i < tokenMark.length(); i++) {
						c = stream.read();
						if (c == -1)
							throw new EOFException();
						if (tokenMark.charAt(i) == (char) c) {
							// pass
						} else {
							ok = false;
							break;
						}
					}
					if (!ok)
						continue;
					StringBuffer buf = new StringBuffer();
					while (true) {
						c = stream.read();
						if (c == -1)
							throw new EOFException();
						if (c == '"')
							break;
						buf.append((char) c);
					}
					token = buf.toString();
					break;
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
				if (raw != null)
					raw.close();
				if (hc != null)
					hc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return false;
	}

}

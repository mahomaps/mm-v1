package mahomaps.api;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.Settings;

public abstract class YmapsApiBase {

	private final String tokenMark = "token\":\"";

	private final Hashtable cookies = new Hashtable();

	protected final String GetToken(String key) throws Exception {
		HttpConnection hc = null;
		InputStream raw = null;
		InputStreamReader stream = null;
		String url = "https://api-maps.yandex.ru/2.1/?lang=ru_RU&apikey=" + key;
		if (Settings.proxyApi) {
			url = Settings.proxyServer + YmapsApiBase.EncodeUrl(url);
		}
		try {
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			hc.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0");
			int r = hc.getResponseCode();
			AcceptCookies(hc);
			raw = hc.openInputStream();
			if (r != 200) {
				ByteArrayOutputStream o = new ByteArrayOutputStream();
				byte[] buf = new byte[256];
				int len;
				while ((len = raw.read(buf)) != -1) {
					o.write(buf, 0, len);
				}
				String str = new String(o.toByteArray(), "UTF-8");
				o.close();
				throw new IOException("Http code: " + r + "\nResponse: " + str);
			}
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
					return buf.toString();
				}
			}
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
	}

	protected String GetUtf(String url) throws IOException, Http403Exception, SecurityException {
		if (Settings.proxyApi) {
			url = Settings.proxyServer + YmapsApiBase.EncodeUrl(url);
		}
		System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) Connector.open(url);
		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			hc.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0");
			SendCookies(hc);
			int r = hc.getResponseCode();
			AcceptCookies(hc);
			if (r == 403 || r == 401)
				throw new Http403Exception();
			is = hc.openInputStream();
			o = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
				o.write(buf, 0, len);
			}
			String str = new String(o.toByteArray(), "UTF-8");
			if (r != 200)
				throw new IOException("Code: " + r + "\n" + str);
			return str;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		} finally {
			if (is != null)
				is.close();
			if (hc != null)
				hc.close();
			if (o != null)
				o.close();
		}
	}

	private void AcceptCookies(HttpConnection hc) throws IOException {
		for (int i = 0; i < 100; i++) {
			String h = hc.getHeaderFieldKey(i);
			if (h == null) {
				if (i > 2)
					break;
				continue;
			}
			if ("Set-Cookie".equals(h)) {
				String val = hc.getHeaderField(i);
				if (val != null) {
					val = val.substring(0, val.indexOf(';'));
					int fec = val.indexOf('=');
					String id = val.substring(0, fec);
					String content = val.substring(fec + 1);
					cookies.put(id, content);
				}
			}
		}
	}

	private void SendCookies(HttpConnection hc) throws IOException {
		System.out.println("Sending " + cookies.size() + " cookies");
		StringBuffer buf = new StringBuffer(cookies.size() * 100);
		boolean first = true;
		Enumeration keys = cookies.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			String val = (String) cookies.get(key);
			if (first) {
				first = false;
			} else {
				buf.append("; ");
			}
			buf.append(key);
			buf.append("=");
			buf.append(val);
		}
		hc.setRequestProperty("Cookie", buf.toString());
	}

	protected void LoadCookies(JSONObject j) {
		JSONArray names = j.keysAsArray();
		for (int i = 0; i < names.size(); i++) {
			String key = names.getString(i);
			cookies.put(key, j.getString(key));
		}
	}

	protected JSONObject SaveCookies() {
		return new JSONObject(cookies);
	}

	public static String EncodeUrl(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if ((65 <= ch) && (ch <= 90)) {
				sbuf.append((char) ch);
			} else if ((97 <= ch) && (ch <= 122)) {
				sbuf.append((char) ch);
			} else if ((48 <= ch) && (ch <= 57)) {
				sbuf.append((char) ch);
			} else if (ch == 32) {
				sbuf.append("%20");
			} else if ((ch == 45) || (ch == 95) || (ch == 46) || (ch == 33) || (ch == 126) || (ch == 42) || (ch == 39)
					|| (ch == 40) || (ch == 41) || (ch == 58) || (ch == 47)) {
				sbuf.append((char) ch);
			} else if (ch <= 127) {
				sbuf.append(hex(ch));
			} else if (ch <= 2047) {
				sbuf.append(hex(0xC0 | ch >> 6));
				sbuf.append(hex(0x80 | ch & 0x3F));
			} else {
				sbuf.append(hex(0xE0 | ch >> 12));
				sbuf.append(hex(0x80 | ch >> 6 & 0x3F));
				sbuf.append(hex(0x80 | ch & 0x3F));
			}
		}
		return sbuf.toString();
	}

	private static String hex(int ch) {
		String x = Integer.toHexString(ch);
		return "%" + (x.length() == 1 ? "0" : "") + x;
	}
}

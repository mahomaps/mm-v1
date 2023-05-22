package mahomaps;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONObject;

import mahomaps.api.YmapsApiBase;
import mahomaps.screens.UpdateScreen;

public class UpdateCheckThread extends Thread {

	public void run() {
		String dev = System.getProperty("microedition.platform");
		if (dev == null)
			dev = "";
		String url = "http://nnp.nnchan.ru/mahomaps/check.php?v=1." + MahoMapsApp.version + "&gt=" + Settings.geoLook
				+ "&device=" + YmapsApiBase.EncodeUrl(dev);
		System.out.println("GET " + url);
		HttpConnection hc = null;
		InputStream is = null;
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		try {
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			is = hc.openInputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
				o.write(buf, 0, len);
			}
			String str = new String(o.toByteArray(), "UTF-8");
			o.close();
			if (r == 200) {
				JSONObject j = new JSONObject(str);
				String uurl = j.optString("url", null);
				String utext = j.optString("text", null);
				if (uurl != null || utext != null) {
					MahoMapsApp.BringSubScreen(new UpdateScreen(utext, uurl));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
				if (hc != null)
					hc.close();
			} catch (Exception e) {
			}
		}
	}
}

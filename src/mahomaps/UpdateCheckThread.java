/*
Copyright (c) 2023 Fyodor Ryzhov
Copyright (c) 2023 Arman Jussupgaliyev
*/
package mahomaps;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import cc.nnproject.json.*;

import mahomaps.api.YmapsApiBase;
import mahomaps.screens.UpdateScreen;

public class UpdateCheckThread extends Thread {

	public UpdateCheckThread() {
		super("Update check");
	}

	public void run() {
		String dev = MahoMapsApp.platform;
		String os = System.getProperty("os.name");
		String osver = System.getProperty("os.version");
		String sejp = System.getProperty("com.sonyericsson.java.platform");
		if (MahoMapsApp.IsKemulator()) {
			dev += "|KEmulator";
			String kemV = System.getProperty("kemulator.mod.version");
			if (kemV != null)
				dev += "/nnmod" + kemV;
		}
		try {
			Class.forName("javax.microedition.shell.MicroActivity");
			dev += "|J2MELoader";
		} catch (Exception e) {
		}
		if (os != null) {
			dev += "|o:" + os;
		}
		if (osver != null) {
			dev += "|v:" + osver;
		}
		if (sejp != null) {
			dev += "|s:" + sejp;
		}
		boolean hasGeo = false;
		try {
			Class.forName("javax.microedition.location.LocationProvider");
			hasGeo = true;
		} catch (Exception e) {
		}
		String url = "http://nnp.nnchan.ru:80/mahomaps/check.php?v=1." + MahoMapsApp.version + "&gt=" + Settings.geoLook
				+ "&geo=" + (hasGeo ? 1 : 0) + "&uf=" + Settings.usageFlags + "&device=" + YmapsApiBase.EncodeUrl(dev);
		System.out.println("GET " + url);
		HttpConnection hc = null;
		InputStream is = null;
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		try {
			hc = (HttpConnection) Connector.open(url + MahoMapsApp.getConnectionParams());
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			is = hc.openInputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
				o.write(buf, 0, len);
			}
			Settings.usageFlags = 0;
			Settings.Save();
			String str = new String(o.toByteArray(), "UTF-8");
			o.close();
			if (r == 200) {
				JSONObject j = JSON.getObject(str);
				String uurl = j.getString("url", null);
				String utext = j.getString("text", null);
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

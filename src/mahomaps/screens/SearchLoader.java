/*
Copyright (c) 2023 Fyodor Ryzhov
Copyright (c) 2023-2024 Arman Jussupgaliyev
*/
package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.*;

import mahomaps.MahoMapsApp;
import mahomaps.api.Http403Exception;
import mahomaps.map.Geopoint;

public class SearchLoader extends Form implements Runnable, CommandListener {

	private Thread th;
	public final String query;
	public final Geopoint point;
	private int tries;

	public SearchLoader(String query, Geopoint point) {
		super(query);
		this.query = query;
		this.point = point;
		setCommandListener(this);
		th = new Thread(this, "Search API request");
		th.start();
	}

	public void run() {
		append(new Gauge(MahoMapsApp.text[14], false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
		try {
			if (tries != 0) {
				MahoMapsApp.api.RefreshToken();
			}
			JSONArray arr = MahoMapsApp.api.Search(query, point, 0.1d);
			MahoMapsApp.BringSubScreen(new SearchScreen(query, point, arr));
		} catch (IOException e) {
			deleteAll();
			append(new StringItem(MahoMapsApp.text[111], MahoMapsApp.text[159]));
			append(new StringItem(MahoMapsApp.text[24], e.getMessage()));
			e.printStackTrace();
		} catch (Http403Exception e) {
			deleteAll();
			if (tries++ == 0) {
				run();
				return;
			}
			append(new StringItem(MahoMapsApp.text[135], MahoMapsApp.text[136]));
		} catch (Exception e) {
			deleteAll();
			append(new StringItem(e.getClass().getName(), e.getMessage()));
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			deleteAll();
			append(new StringItem(MahoMapsApp.text[121], MahoMapsApp.text[160]));
		}
		addCommand(MahoMapsApp.back);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMap();
		}
	}

}

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
import mahomaps.api.AccessErrorException;
import mahomaps.map.Geopoint;

public class SearchLoader extends Form implements Runnable, CommandListener {

	private Thread th;
	public final String query;
	public final Geopoint point;

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
			JSONArray arr = MahoMapsApp.api.Search(query, point, 0.1d);
			MahoMapsApp.BringSubScreen(new SearchScreen(query, point, arr));
		} catch (IOException e) {
			deleteAll();
			append(new StringItem(MahoMapsApp.text[111], MahoMapsApp.text[159]));
			append(new StringItem(MahoMapsApp.text[24], e.getMessage()));
			e.printStackTrace();
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

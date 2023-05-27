package mahomaps.screens;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import org.json.me.JSONArray;

import mahomaps.MahoMapsApp;
import mahomaps.api.Http403Exception;
import mahomaps.map.Geopoint;

public class SearchLoader extends Form implements Runnable, CommandListener {

	private Thread th;
	public final String query;
	public final Geopoint point;
	private Command back = new Command("Назад", Command.BACK, 0);

	public SearchLoader(String query, Geopoint point) {
		super(query);
		this.query = query;
		this.point = point;
		setCommandListener(this);
		th = new Thread(this, "Search API request");
		th.start();
	}

	public void run() {
		append(new Gauge("Подключение к серверу", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
		try {
			JSONArray arr = MahoMapsApp.api.Search(query, point, 0.1d);
			MahoMapsApp.BringSubScreen(new SearchScreen(query, point, arr));
		} catch (IOException e) {
			deleteAll();
			append(new StringItem("Сетевая ошибка", "Проверьте подключение к интернету."));
			e.printStackTrace();
		} catch (Http403Exception e) {
			deleteAll();
			append(new StringItem("Отказ в доступе к API", "Попробуйте сбросить сессию в меню."));
		} catch (Exception e) {
			deleteAll();
			append(new StringItem(e.getClass().getName(), e.getMessage()));
			e.printStackTrace();
		}
		addCommand(back);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMap();
		}
	}

}

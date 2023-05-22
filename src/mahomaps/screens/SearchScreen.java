package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.overlays.SearchOverlay;

public class SearchScreen extends List implements Runnable, CommandListener {

	private Thread th;
	public final String query;
	private Geopoint point;
	public JSONArray results;
	private SearchOverlay overlay;

	public boolean onePointFocused = false;

	private Command reset = new Command("Сброс", Command.BACK, 0);
	private Command toMap = new Command("К карте", Command.SCREEN, 0);

	public SearchScreen(String query, Geopoint point) {
		super("Результаты поиска", Choice.IMPLICIT);
		this.query = query;
		this.point = point;
		th = new Thread(this, "search");
		th.start();
	}

	public void run() {
		try {
			setTitle("Загрузка...");
			JSONArray arr = MahoMapsApp.api.Search(query, point, 0.1d);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				JSONObject props = obj.getJSONObject("properties");
				append(props.getString("name") + "\n" + props.optString("description", ""), null);
			}
			results = arr;
			MahoMapsApp.lastSearch = this;
			overlay = new SearchOverlay(point, query, results, this);
			MahoMapsApp.Overlays().PushOverlay(overlay);
			setTitle(query);
			addCommand(reset);
			addCommand(toMap);
			setCommandListener(this);
		} catch (Exception e) {
			e.printStackTrace();
			setTitle("Ошибка");
			addCommand(reset);
			setCommandListener(this);
		}
	}

	public static void ResetSearch() {
		MahoMapsApp.lastSearch = null;
		MahoMapsApp.Overlays().CloseOverlay(SearchOverlay.ID);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == reset) {
			ResetSearch();
			MahoMapsApp.BringMap();
		} else if (c == toMap) {
			MahoMapsApp.BringMap();
		} else if (c == SELECT_COMMAND) {
			overlay.SetSelection(results.getJSONObject(getSelectedIndex()));
			MahoMapsApp.BringSubScreen(new SearchResultScreen(results.getJSONObject(getSelectedIndex()), overlay));
		}
	}

}

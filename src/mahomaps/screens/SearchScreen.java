package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class SearchScreen extends List implements Runnable, CommandListener, IButtonHandler {

	private Thread th;
	public final String query;
	private Geopoint point;
	public JSONArray results;

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
		MahoMapsApp.GetCanvas().SetOverlayContent(null);
		MahoMapsApp.GetCanvas().searchPoints.removeAllElements();
	}

	public void SetPoints() {
		Vector p = MahoMapsApp.GetCanvas().searchPoints;
		p.removeAllElements();
		for (int i = 0; i < results.length(); i++) {
			JSONArray point1 = results.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
			Geopoint gp = new Geopoint(point1.getDouble(1), point1.getDouble(0));
			gp.type = Geopoint.POI_SEARCH;
			gp.color = Geopoint.COLOR_GREEN;
			p.addElement(gp);
		}
	}

	public void SetUI() {

		ColumnsContainer cols = new ColumnsContainer(
				new UIElement[] { new Button("К списку", 1, this, 5), new Button("Закрыть", 2, this, 5) });
		FillFlowContainer flow = new FillFlowContainer(new UIElement[] { new SimpleText(query, 0),
				new SimpleText("Найдено: " + results.length(), 0), new SimpleText("Ничего не выбрано.", 0), cols });
		MahoMapsApp.GetCanvas().SetOverlayContent(flow);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == reset) {
			ResetSearch();
			MahoMapsApp.BringMenu();
		} else if (c == toMap) {
			MahoMapsApp.lastSearch = this;
			SetPoints();
			SetUI();
			MahoMapsApp.BringMap();
		} else if (c == SELECT_COMMAND) {
			MahoMapsApp.lastSearch = this;
			MahoMapsApp.BringSubScreen(new SearchResultScreen(results.getJSONObject(getSelectedIndex())));
		}
	}

	public void OnButtonTap(UIElement sender, int uid) {
		if (uid == 1) {
			MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
		} else if (uid == 2) {
			ResetSearch();
		}
	}

}

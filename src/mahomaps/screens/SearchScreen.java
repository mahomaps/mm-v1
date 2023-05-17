package mahomaps.screens;

import java.util.Vector;

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
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class SearchScreen extends List implements Runnable, CommandListener {

	private Thread th;
	public final String query;
	private Geopoint point;
	public JSONArray results;

	private Command reset = new Command("Сброс", Command.BACK, 0);
	private Command toMap = new Command("К карте", Command.SCREEN, 0);

	public SearchScreen(String query, Geopoint point) {
		super("Результаты поиска", List.IMPLICIT);
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

	public void commandAction(Command c, Displayable d) {
		if (c == reset) {
			MahoMapsApp.lastSearch = null;
			MahoMapsApp.BringMenu();
			MahoMapsApp.GetCanvas().SetOverlayContent(null);
			MahoMapsApp.GetCanvas().searchPoints.removeAllElements();
		} else if (c == toMap) {
			MahoMapsApp.lastSearch = this;
			Vector p = MahoMapsApp.GetCanvas().searchPoints;
			p.removeAllElements();
			for (int i = 0; i < results.length(); i++) {
				JSONArray point = results.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
				Geopoint gp = new Geopoint(point.getDouble(1), point.getDouble(0));
				gp.type = Geopoint.POI_SEARCH;
				gp.color = Geopoint.COLOR_GREEN;
				p.addElement(gp);
			}
			{
				FillFlowContainer flow = new FillFlowContainer(new UIElement[] { new SimpleText(query, 0),
						new SimpleText("Найдено: " + results.length(), 0), new SimpleText("Ничего не выбрано.", 0) });
				Button b1 = new Button("К списку", 0, null, 5);
				b1.W = 100;
				Button b2 = new Button("Закрыть", 0, null, 5);
				b2.W = 100;
				ColumnsContainer cols = new ColumnsContainer(new UIElement[] { flow, b1, b2 });
				cols.stretchableElement = 0;
				MahoMapsApp.GetCanvas().SetOverlayContent(cols);
			}
			MahoMapsApp.BringMap();
		} else if (c == SELECT_COMMAND) {
			MahoMapsApp.lastSearch = this;
			MahoMapsApp.BringSubScreen(new SearchResultScreen(results.getJSONObject(getSelectedIndex())));
		}
	}

}

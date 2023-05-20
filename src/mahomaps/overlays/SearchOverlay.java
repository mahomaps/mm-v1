package mahomaps.overlays;

import java.util.Vector;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.screens.SearchResultScreen;
import mahomaps.screens.SearchScreen;
import mahomaps.ui.Button;
import mahomaps.ui.ColumnsContainer;
import mahomaps.ui.FillFlowContainer;
import mahomaps.ui.IButtonHandler;
import mahomaps.ui.SimpleText;
import mahomaps.ui.UIElement;

public class SearchOverlay extends MapOverlay implements IButtonHandler {

	public static final String ID = "search";
	private final String query;
	private final JSONArray results;
	private Vector points;
	private Geopoint selected = null;
	private SearchScreen list;

	public SearchOverlay(Geopoint around, String query, JSONArray results, SearchScreen list) {
		this.query = query;
		this.results = results;
		this.list = list;

		SetNullSelection();

	}

	public void SetNullSelection() {
		selected = null;
		points = new Vector();
		for (int i = 0; i < results.length(); i++) {
			JSONArray point1 = results.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
			Geopoint gp = new Geopoint(point1.getDouble(1), point1.getDouble(0));
			gp.type = Geopoint.POI_SEARCH;
			gp.color = Geopoint.COLOR_GREEN;
			gp.object = results.getJSONObject(i);
			points.addElement(gp);
		}

		content = new FillFlowContainer(
				new UIElement[] { new SimpleText(query, 0), new SimpleText("Найдено: " + results.length(), 0),
						new SimpleText("Ничего не выбрано.", 0), new ColumnsContainer(
								new UIElement[] { new Button("Список", 1, this), new Button("Закрыть", 0, this) }) });
	}

	private void SetSelection(Geopoint p) {
		selected = p;
		for (int i = 0; i < points.size(); i++) {
			Geopoint pp = (Geopoint) points.elementAt(i);
			if (pp == p) {
				points.removeElementAt(i);
				i--;
				pp.color = Geopoint.COLOR_RED;
			} else {
				pp.color = Geopoint.COLOR_GRAY;
			}
		}
		points.addElement(p);

		JSONObject data = (JSONObject) p.object;

		content = new FillFlowContainer(new UIElement[] { new SimpleText(data.optString("name"), 0),
				new SimpleText(data.optString("description"), 0),
				new ColumnsContainer(
						new UIElement[] { new Button("Карточка", 2, this), new Button("Показать", 3, this) }),
				new ColumnsContainer(
						new UIElement[] { new Button("Точка А", 4, this), new Button("Точка Б", 5, this) }),
				new Button("Назад", 6, this) });

	}

	public void SetSelection(JSONObject obj) {
		for (int i = 0; i < points.size(); i++) {
			Geopoint p = (Geopoint) points.elementAt(i);
			if (p.object == obj) {
				SetSelection(p);
				return;
			}
		}
	}

	public String GetId() {
		return ID;
	}

	public Vector GetPoints() {
		return points;
	}

	public boolean OnPointTap(Geopoint p) {
		if (p.color == Geopoint.COLOR_RED || p.object == null)
			return false;
		SetSelection(p);
		return true;
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case 0:
			SearchScreen.ResetSearch();
			Close();
			break;
		case 1:
			MahoMapsApp.BringSubScreen(list);
			break;
		case 2:
			MahoMapsApp.BringSubScreen(new SearchResultScreen((JSONObject) selected.object, this));
			break;
		case 3:
			MahoMapsApp.GetCanvas().state = MapState.FocusAt(selected);
			break;
		case 4:
			break;
		case 5:
			break;
		case 6:
			SetNullSelection();
			break;
		}
	}

}

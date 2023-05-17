package mahomaps.screens;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

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

public class SearchResultScreen extends Form implements CommandListener, IButtonHandler {

	private Command back = new Command("К списку", Command.BACK, 0);
	private Command toMap = new Command("К карте", Command.OK, 0);
	private final JSONObject obj;
	private final String name;
	private final String descr;

	public SearchResultScreen(JSONObject obj) {
		super("Результат поиска");
		this.obj = obj;
		JSONObject props = obj.getJSONObject("properties");
		JSONArray point = obj.getJSONObject("geometry").getJSONArray("coordinates");
		name = props.optString("name");
		append(new StringItem("Название", name));
		descr = props.optString("description", null);
		if (descr != null)
			append(new StringItem("Описание", descr));
		append(new StringItem("Координаты", point.getDouble(1) + " " + point.getDouble(0)));
		JSONObject org = props.optJSONObject("CompanyMetaData");
		if (org != null) {
			JSONObject hours = org.optJSONObject("Hours");
			if (hours != null)
				append(new StringItem("Режим работы", hours.optString("text")));
			append(new StringItem("Сайт", org.optString("url")));
			JSONArray phones = org.optJSONArray("Phones");
			if (phones != null && phones.length() != 0)
				append(new StringItem("Телефон", phones.getJSONObject(0).optString("formatted")));
		}

		addCommand(back);
		addCommand(toMap);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
		} else if (c == toMap) {
			JSONArray results = MahoMapsApp.lastSearch.results;
			Vector p = MahoMapsApp.GetCanvas().searchPoints;
			p.removeAllElements();
			for (int i = 0; i < results.length(); i++) {
				JSONObject robj = results.getJSONObject(i);
				if (robj == obj)
					continue;
				JSONArray point = robj.getJSONObject("geometry").getJSONArray("coordinates");
				Geopoint gp = new Geopoint(point.getDouble(1), point.getDouble(0));
				gp.type = Geopoint.POI_SEARCH;
				gp.color = Geopoint.COLOR_GRAY;
				p.addElement(gp);
			}
			{
				JSONArray point = obj.getJSONObject("geometry").getJSONArray("coordinates");
				Geopoint gp = new Geopoint(point.getDouble(1), point.getDouble(0));
				gp.type = Geopoint.POI_SEARCH;
				gp.color = Geopoint.COLOR_RED;
				p.addElement(gp);
			}
			{
				FillFlowContainer flow = new FillFlowContainer(
						new UIElement[] { new SimpleText(name, 0), new SimpleText(descr, 0),
								new ColumnsContainer(new UIElement[] { new Button("Точка А", 2, this, 5),
										new Button("Точка Б", 3, this, 5) }),
								new ColumnsContainer(new UIElement[] { new Button("Карточка", 1, this, 5),
										new Button("К списку", 4, this, 5), new Button("Ко всем", 5, this, 5) }) });

				MahoMapsApp.GetCanvas().SetOverlayContent(flow);
			}
			MahoMapsApp.BringMap();
		}
	}

	public void OnButtonTap(UIElement sender, int uid) {
		switch (uid) {
		case 1:
			MahoMapsApp.BringSubScreen(this);
			break;
		case 2:
		case 3:
			break;
		case 4:
			MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
			break;
		case 5:
			MahoMapsApp.lastSearch.SetPoints();
			MahoMapsApp.lastSearch.SetUI();
			break;
		}
	}
}

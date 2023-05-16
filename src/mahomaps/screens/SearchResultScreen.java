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

public class SearchResultScreen extends Form implements CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);
	private Command toMap = new Command("К карте", Command.OK, 0);
	private final JSONObject obj;

	public SearchResultScreen(JSONObject obj) {
		super("Результат поиска");
		this.obj = obj;
		JSONObject props = obj.getJSONObject("properties");
		JSONArray point = obj.getJSONObject("geometry").getJSONArray("coordinates");
		append(new StringItem("Название", props.optString("name")));
		String descr = props.optString("description", null);
		if (descr != null)
			append(new StringItem("Описание", descr));
		append(new StringItem("Координаты", point.getDouble(1) + " " + point.getDouble(0)));
		JSONObject org = props.optJSONObject("CompanyMetaData");
		if (org != null) {
			append(new StringItem("Режим работы", org.getJSONObject("Hours").optString("text")));
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
			Vector p = MahoMapsApp.GetCanvas().searchPoints;
			p.removeAllElements();
			JSONArray point = obj.getJSONObject("geometry").getJSONArray("coordinates");
			Geopoint gp = new Geopoint(point.getDouble(1), point.getDouble(0));
			gp.type = Geopoint.POI_SEARCH;
			p.addElement(gp);

			MahoMapsApp.BringMap();
		}
	}
}

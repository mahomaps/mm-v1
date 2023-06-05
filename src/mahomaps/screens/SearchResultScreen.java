package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import mahomaps.MahoMapsApp;
import mahomaps.overlays.SearchOverlay;

public class SearchResultScreen extends Form implements CommandListener {

	private Command back = new Command("Список", Command.BACK, 0);
	private final SearchOverlay o;

	public SearchResultScreen(JSONObject obj, SearchOverlay o) {
		super("Результат поиска");
		this.o = o;
		JSONObject props = obj.getJSONObject("properties");
		JSONArray point = obj.getJSONObject("geometry").getJSONArray("coordinates");
		String name = props.optString("name");
		append(new StringItem("Название", name));
		String descr = props.optString("description", null);
		if (descr != null)
			append(new StringItem("Описание", descr));
		append(new StringItem("Координаты", point.getDouble(1) + " " + point.getDouble(0)));
		JSONObject org = props.optJSONObject("CompanyMetaData");
		if (org != null) {
			JSONObject hours = org.optJSONObject("Hours");
			if (hours != null)
				append(new StringItem("Режим работы", hours.optString("text")));
			if (org.optString("url", null) != null)
				append(new StringItem("Сайт", org.optString("url")));
			JSONArray phones = org.optJSONArray("Phones");
			if (phones != null && phones.length() != 0)
				append(new StringItem("Телефон", phones.getJSONObject(0).optString("formatted")));
		}

		addCommand(back);
		addCommand(MahoMapsApp.toMap);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			o.SetNullSelection();
			MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
		} else if (c == MahoMapsApp.toMap) {
			MahoMapsApp.BringMap();
		}
	}
}

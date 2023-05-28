package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.MahoMapsApp;
import mahomaps.overlays.SearchOverlay;

public class SearchResultScreen extends Form implements CommandListener {

	private Command back = new Command("Список", Command.BACK, 0);
	private Command toMap = new Command("Карта", Command.OK, 0);
	private final SearchOverlay o;

	public SearchResultScreen(JSONObject obj, SearchOverlay o) {
		super("Результат поиска");
		this.o = o;
		JSONObject props = obj.getObject("properties");
		JSONArray point = obj.getObject("geometry").getArray("coordinates");
		String name = props.getNullableString("name");
		append(new StringItem("Название", name));
		String descr = props.getString("description", null);
		if (descr != null)
			append(new StringItem("Описание", descr));
		append(new StringItem("Координаты", point.getDouble(1) + " " + point.getDouble(0)));
		JSONObject org = props.getNullableObject("CompanyMetaData");
		if (org != null) {
			JSONObject hours = org.getNullableObject("Hours");
			if (hours != null)
				append(new StringItem("Режим работы", hours.getNullableString("text")));
			if (org.getString("url", null) != null)
				append(new StringItem("Сайт", org.getNullableString("url")));
			JSONArray phones = org.getNullableArray("Phones");
			if (phones != null && phones.size() != 0)
				append(new StringItem("Телефон", phones.getObject(0).getNullableString("formatted")));
		}

		addCommand(back);
		addCommand(toMap);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			o.SetNullSelection();
			MahoMapsApp.BringSubScreen(MahoMapsApp.lastSearch);
		} else if (c == toMap) {
			MahoMapsApp.BringMap();
		}
	}
}

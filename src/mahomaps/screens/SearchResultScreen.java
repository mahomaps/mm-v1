package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
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
		StringItem nameItem = new StringItem("Название", name);
		nameItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(nameItem);
		String descr = props.optString("description", null);
		if (descr != null) {
			StringItem descrItem = new StringItem("Описание", descr);
			descrItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
			append(descrItem);
		}
		StringItem coordsItem = new StringItem("Координаты", point.getDouble(1) + " " + point.getDouble(0));
		coordsItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(coordsItem);
		JSONObject org = props.optJSONObject("CompanyMetaData");
		if (org != null) {
			JSONObject hours = org.optJSONObject("Hours");
			if (hours != null) {
				StringItem hoursItem = new StringItem("Режим работы", hours.optString("text"));
				hoursItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
				append(hoursItem);
			}
			if (org.optString("url", null) != null) {
				StringItem urlItem = new StringItem("Сайт", org.optString("url"));
				urlItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
				append(urlItem);
			}
			JSONArray phones = org.optJSONArray("Phones");
			if (phones != null && phones.length() != 0) {
				StringItem phonesItem = new StringItem("Телефон", phones.getJSONObject(0).optString("formatted"));
				phonesItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
				append(phonesItem);
			}
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

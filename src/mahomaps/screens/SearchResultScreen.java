package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.*;

import mahomaps.MahoMapsApp;
import mahomaps.overlays.SearchOverlay;

public class SearchResultScreen extends Form implements CommandListener {

	private Command back = new Command("Список", Command.BACK, 0);
	private final SearchOverlay o;

	public SearchResultScreen(JSONObject obj, SearchOverlay o) {
		super("Результат поиска");
		this.o = o;
		JSONObject props = obj.getObject("properties");
		JSONArray point = obj.getObject("geometry").getArray("coordinates");
		String name = props.getNullableString("name");
		StringItem nameItem = new StringItem(MahoMapsApp.text[161], name);
		nameItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(nameItem);
		String descr = props.getString("description", null);
		if (descr != null) {
			StringItem descrItem = new StringItem(MahoMapsApp.text[162], descr);
			descrItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
			append(descrItem);
		}
		StringItem coordsItem = new StringItem(MahoMapsApp.text[163], point.getDouble(1) + " " + point.getDouble(0));
		coordsItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		append(coordsItem);
		JSONObject org = props.getNullableObject("CompanyMetaData");
		if (org != null) {
			JSONObject hours = org.getNullableObject("Hours");
			if (hours != null) {
				StringItem hoursItem = new StringItem(MahoMapsApp.text[164], hours.getNullableString("text"));
				hoursItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
				append(hoursItem);
			}
			if (org.getNullableString("url") != null) {
				StringItem urlItem = new StringItem(MahoMapsApp.text[165], org.getNullableString("url"));
				urlItem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
				append(urlItem);
			}
			JSONArray phones = org.getNullableArray("Phones");
			if (phones != null && phones.size() != 0) {
				StringItem phonesItem = new StringItem(MahoMapsApp.text[166], phones.getObject(0).getNullableString("formatted"));
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

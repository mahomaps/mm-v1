package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import cc.nnproject.json.*;

import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.overlays.SearchOverlay;

public class SearchScreen extends List implements CommandListener {

	public final String query;
	public JSONArray results;
	private SearchOverlay overlay;

	public boolean onePointFocused = false;

	private Command reset = new Command(MahoMapsApp.text[5], Command.BACK, 0);

	public SearchScreen(String query, Geopoint point, JSONArray results) {
		super(query, Choice.IMPLICIT);
		setFitPolicy(TEXT_WRAP_ON);
		this.query = query;
		this.results = results;
		for (int i = 0; i < results.size(); i++) {
			JSONObject obj = results.getObject(i);
			JSONObject props = obj.getObject("properties");
			append(props.getString("name") + (props.has("description") && !props.isNull("description") ? " (" + props.getString("description", "") + ")" : ""), null);
		}
		MahoMapsApp.lastSearch = this;
		overlay = new SearchOverlay(point, query, results, this);
		MahoMapsApp.Overlays().PushOverlay(overlay);
		addCommand(reset);
		addCommand(MahoMapsApp.toMap);
		setCommandListener(this);
	}

	public static void ResetSearch() {
		MahoMapsApp.lastSearch = null;
		MahoMapsApp.Overlays().CloseOverlay(SearchOverlay.ID);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == reset) {
			ResetSearch();
			MahoMapsApp.BringMap();
		} else if (c == MahoMapsApp.toMap) {
			MahoMapsApp.BringMap();
		} else if (c == SELECT_COMMAND) {
			overlay.SetSelection(results.getObject(getSelectedIndex()));
			MahoMapsApp.BringSubScreen(new SearchResultScreen(results.getObject(getSelectedIndex()), overlay));
		}
	}

}

package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;
import mahomaps.map.MapState;
import mahomaps.overlays.RouteBuildOverlay;

public class BookmarksScreen extends List implements CommandListener {

	public final static String RMS_NAME = "mm_v1_bookmarks";

	private JSONArray list;

	private Command from = new Command(MahoMapsApp.text[104], Command.ITEM, 0);
	private Command to = new Command(MahoMapsApp.text[105], Command.ITEM, 1);
	private Command del = new Command(MahoMapsApp.text[149], Command.ITEM, 2);
	private Command rename = new Command(MahoMapsApp.text[150], Command.ITEM, 2);

	public BookmarksScreen() {
		super(MahoMapsApp.text[148], Choice.IMPLICIT);
		list = read();
		list.build();
		fillList();
		addCommand(MahoMapsApp.back);
		if (list.size() > 0) {
			addCommand(from);
			addCommand(to);
			addCommand(del);
			addCommand(rename);
		}
		setCommandListener(this);
	}

	private void fillList() {
		for (int i = 0; i < list.size(); i++) {
			append(list.getObject(i).getString("name", "Not named"), null);
		}
	}

	private static JSONArray read() {
		try {
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);
			byte[] d = null;
			if (r.getNumRecords() > 0) {
				d = r.getRecord(1);
			}
			r.closeRecordStore();

			if (d == null)
				return JSON.getArray("[]");

			return JSON.getArray(new String(d, "UTF-8"));
		} catch (Throwable e) {
			return JSON.getArray("[]");
		}
	}

	public static void BeginAdd(final Geopoint p, String defaultName) {
		final TextBox tb = new TextBox(MahoMapsApp.text[151], defaultName == null ? "" : defaultName, 100, 0);
		tb.addCommand(MahoMapsApp.back);
		tb.addCommand(MahoMapsApp.ok);
		tb.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == MahoMapsApp.back)
					MahoMapsApp.BringMap();
				else if (c == MahoMapsApp.ok) {
					String text = tb.getString();
					if (text != null && text.length() > 0) {
						Add(p, text);
					}
					MahoMapsApp.BringMap();
				}
			}
		});
		MahoMapsApp.BringSubScreen(tb);
	}

	public static void Add(Geopoint p, String name) {
		JSONArray arr = read();
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("lat", p.lat);
		obj.put("lon", p.lon);
		arr.add(obj);
		Save(arr);
	}

	private static void Save(JSONArray arr) {
		try {
			byte[] d = arr.toString().getBytes("UTF-8");
			RecordStore r = RecordStore.openRecordStore(RMS_NAME, true);
			if (r.getNumRecords() == 0)
				r.addRecord(new byte[1], 0, 1);
			r.setRecord(1, d, 0, d.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			if (d instanceof TextBox) {
				MahoMapsApp.BringSubScreen(this);
				return;
			}
			MahoMapsApp.BringMap();
			return;
		}

		if (list.size() == 0)
			return;
		int n = getSelectedIndex();
		if (n == -1)
			return;

		if (c == MahoMapsApp.ok) {
			 // будем надеяться что фокус элемента не сбросится пока юзер будет вводить текст
			String s = ((TextBox) d).getString();
			set(n, s, null);
			list.getObject(n).put("name", s);
			return;
		}
		if (c == del) {
			list.remove(n);
			delete(n);
			Save(list);
			return;
		}
		if (c == rename) {
			final TextBox tb = new TextBox(MahoMapsApp.text[151], getString(n), 100, 0);
			tb.addCommand(MahoMapsApp.back);
			tb.addCommand(MahoMapsApp.ok);
			tb.setCommandListener(this);
			MahoMapsApp.BringSubScreen(tb);
			return;
		}
		JSONObject obj = list.getObject(n);
		Geopoint p = new Geopoint(obj.getDouble("lat"), obj.getDouble("lon"));
		if (c == SELECT_COMMAND) {
			MahoMapsApp.GetCanvas().state = MapState.FocusAt(p);
			MahoMapsApp.BringMap();
		} else if (c == from) {
			RouteBuildOverlay.Get().SetA(p);
			MahoMapsApp.BringMap();
		} else if (c == to) {
			RouteBuildOverlay.Get().SetB(p);
			MahoMapsApp.BringMap();
		}
	}

}

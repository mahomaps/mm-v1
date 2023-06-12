package mahomaps.screens;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import mahomaps.MahoMapsApp;
import mahomaps.map.Geopoint;

public class BookmarksScreen extends List implements CommandListener {

	public final static String RMS_NAME = "mm_v1_bookmarks";

	private JSONArray list;

	public BookmarksScreen() {
		super("Закладки", Choice.IMPLICIT);
		list = read();
		list.build();
		for (int i = 0; i < list.size(); i++) {
			append(list.getObject(i).getString("name", "Not named"), null);
		}
		addCommand(MahoMapsApp.back);
		setCommandListener(this);
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

			return JSON.getArray(new String(d));
		} catch (Throwable e) {
			return JSON.getArray("[]");
		}
	}

	public static void Add(Geopoint p, String name) {
		JSONArray arr = read();
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("lat", p.lat);
		obj.put("lon", p.lon);
		arr.add(obj);
		try {
			byte[] d = arr.toString().getBytes();
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
		if (c == MahoMapsApp.back)
			MahoMapsApp.BringMap();
	}

}

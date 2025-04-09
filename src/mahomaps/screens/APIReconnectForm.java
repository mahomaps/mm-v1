/*
Copyright (c) 2023 Fyodor Ryzhov
*/
package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;
import mahomaps.Settings;

public class APIReconnectForm extends Form implements Runnable, CommandListener, ItemCommandListener {

	public APIReconnectForm() {
		super("MahoMaps v1");
		setCommandListener(this);
		if (MahoMapsApp.api.token == null) {
			StartTokenRefresh();
		} else {
			ShowOk();
		}
	}

	public void run() {
		Thread.yield();
		try {
			MahoMapsApp.api.RefreshToken();
			ShowSuc();
		} catch (Exception e) {
			ShowFail(e);
		}

	}

	private void StartTokenRefresh() {
		removeCommand(MahoMapsApp.back);
		deleteAll();
		append(new Gauge(MahoMapsApp.text[14], false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
		(new Thread(this, "API reconnect")).start();
	}

	private void ShowOk() {
		deleteAll();
		append(new StringItem(MahoMapsApp.text[15], MahoMapsApp.text[16]));
		StringItem b = new StringItem(MahoMapsApp.text[20], MahoMapsApp.text[5], Item.BUTTON);
		b.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
		b.setDefaultCommand(MahoMapsApp.reset);
		b.setItemCommandListener(this);
		append(b);
		addCommand(MahoMapsApp.back);
	}

	private void ShowSuc() {
		deleteAll();
		append(new StringItem(MahoMapsApp.text[15], MahoMapsApp.text[16]));
		addCommand(MahoMapsApp.back);
	}

	private void ShowFail(Exception e) {
		deleteAll();
		if (e instanceof SecurityException) {
			append(new StringItem(MahoMapsApp.text[15], MahoMapsApp.text[21]));
		} else {
			append(new StringItem(MahoMapsApp.text[15], MahoMapsApp.text[22]));
			append(new StringItem(MahoMapsApp.text[19], MahoMapsApp.text[Settings.proxyApi ? 17 : 18]));
			append(new StringItem(MahoMapsApp.text[23], e.getClass().getName()));
			append(new StringItem(MahoMapsApp.text[24], e.getMessage()));
		}
		addCommand(MahoMapsApp.back);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == MahoMapsApp.back) {
			MahoMapsApp.BringMenu();
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == MahoMapsApp.reset) {
			MahoMapsApp.api.token = null;
			MahoMapsApp.api.Save();
			StartTokenRefresh();
		}
	}

}

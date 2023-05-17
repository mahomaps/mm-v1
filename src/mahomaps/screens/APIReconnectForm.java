package mahomaps.screens;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import mahomaps.MahoMapsApp;

public class APIReconnectForm extends Form implements Runnable, CommandListener {

	private Command back = new Command("Назад", Command.BACK, 0);

	public APIReconnectForm() {
		super("MahoMaps v1");
		setCommandListener(this);
		if (MahoMapsApp.api.token == null) {
			append(new Gauge("Подключение", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
			(new Thread(this)).start();
		} else {
			ShowOk();
		}
	}

	public void run() {
		Thread.yield();
		boolean sucs = MahoMapsApp.api.RefreshToken();
		Thread.yield();
		if (sucs)
			ShowOk();
		else
			ShowFail();
	}

	private void ShowOk() {
		deleteAll();
		append(new StringItem("Статус", "Подключено"));
		append(new StringItem("Токен", MahoMapsApp.api.token));
		addCommand(back);
	}

	private void ShowFail() {
		deleteAll();
		append(new StringItem("Статус", "Ошибка. Попытайтесь ещё раз."));
		append(new StringItem("Токен", "-"));
		addCommand(back);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
		}
	}

}

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
		try {
			MahoMapsApp.api.RefreshToken();
			ShowOk();
		} catch (Exception e) {
			ShowFail(e);
		}

	}

	private void ShowOk() {
		deleteAll();
		append(new StringItem("Статус", "Подключено"));
		addCommand(back);
	}

	private void ShowFail(Exception e) {
		deleteAll();
		if (e instanceof SecurityException) {
			append(new StringItem("Статус", "Приложению запрещён доступ в интернет."));
		} else {
			append(new StringItem("Статус", "Ошибка. Попытайтесь ещё раз."));
			append(new StringItem("Возникшее исключение", e.getClass().getName()));
			append(new StringItem("Сведения", e.getMessage()));
		}
		addCommand(back);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			MahoMapsApp.BringMenu();
		}
	}

}

package mahomaps;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import mahomaps.screens.Splash;

public class MahoMapsApp extends MIDlet implements Runnable {

	private static Display display;
	private static Thread thread;

	public MahoMapsApp() {
		display = Display.getDisplay(this);
	}

	protected void startApp() throws MIDletStateChangeException {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	public void run() {
		BringSubScreen(new Splash());
	}

	public void BringSubScreen(Displayable screen) {
		display.setCurrent(screen);
	}
}

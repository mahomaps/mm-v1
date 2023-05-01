package mahomaps;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import mahomaps.map.TilesProvider;
import mahomaps.screens.MapCanvas;
import mahomaps.screens.Splash;

public class MahoMapsApp extends MIDlet implements Runnable {

	private static Display display;
	private static Thread thread;
	private static TilesProvider tiles;
	private static MapCanvas canvas;

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
		thread.interrupt();
	}

	protected void pauseApp() {
	}

	public void run() {
		BringSubScreen(new Splash());
		tiles = new TilesProvider("ru_RU", "file:///root1/ym");
		canvas = new MapCanvas(tiles);
		BringMap();
		while (true) {
			canvas.update();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public static void BringSubScreen(Displayable screen) {
		display.setCurrent(screen);
	}

	public static void BringMap() {
		display.setCurrent(canvas);
	}
}

package mahomaps;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
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
	private static MIDlet midlet;

	public static String version;

	public MahoMapsApp() {
		display = Display.getDisplay(this);
	}

	protected void startApp() throws MIDletStateChangeException {
		if (thread == null) {
			midlet = this;
			thread = new Thread(this);
			thread.start();
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		thread.interrupt();
		if (tiles != null)
			tiles.interrupt();
	}

	protected void pauseApp() {
	}

	public void run() {
		BringSubScreen(new Splash());
		version = getAppProperty("MIDlet-Version");
		try {
			tiles = new TilesProvider("ru_RU", "file:///root/ym/");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		canvas = new MapCanvas(tiles);
		tiles.start();
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

	public static void open(String link) {
		try {
			midlet.platformRequest(link);
		} catch (ConnectionNotFoundException e) {
		}
	}

	private final static double E = 2.71828182845904523536028d;

	public static double ln(double n) {
		int a = 0, b;
		double c, d, e, f;
		if (n < 0)
			c = Double.NaN;
		else if (n != 0) {
			for (; (d = n / E) > 1; ++a, n = d)
				;
			for (; (d = n * E) < 1; --a, n = d)
				;
			d = 1d / (n - 1);
			d = d + d + 1;
			e = d * d;
			c = 0;
			f = 1;
			for (b = 1; c + 0.00000001d < (c += 1d / (b * f)); b += 2) {
				f *= e;
			}
			c *= 2 / d;
		} else
			c = Double.NEGATIVE_INFINITY;
		return a + c;
	}
}

package mahomaps;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import mahomaps.api.YmapsApi;
import mahomaps.map.Geopoint;
import mahomaps.map.TilesProvider;
import mahomaps.screens.MapCanvas;
import mahomaps.screens.MenuScreen;
import mahomaps.screens.SearchScreen;
import mahomaps.screens.Splash;

public class MahoMapsApp extends MIDlet implements Runnable {

	private static Display display;
	public static Thread thread;
	private static TilesProvider tiles;
	private static MapCanvas canvas;
	private static MenuScreen menu;
	private static MIDlet midlet;
	public static SearchScreen lastSearch;
	public static final YmapsApi api = new YmapsApi();

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
		if (canvas != null)
			canvas.dispose();
	}

	protected void pauseApp() {
	}

	public void run() {
		BringSubScreen(new Splash());
		version = getAppProperty("MIDlet-Version");
		try {
			boolean kem = IsKemulator();
			String loc;
			if (kem) {
				loc = "file:///root/ym/";
			} else {
				loc = System.getProperty("fileconn.dir.private");
				if (loc == null)
					loc = System.getProperty("fileconn.dir.images");
				if (loc.charAt(loc.length() - 1) != '/')
					loc = loc + "/";
				loc = loc + "ym/";
			}
			tiles = new TilesProvider("ru_RU", loc);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		api.RefreshToken();
		menu = new MenuScreen();
		canvas = new MapCanvas(tiles);
		tiles.start();
		canvas.points.addElement(new Geopoint(85, 15d));
		canvas.points.addElement(new Geopoint(66.513484, 42.258826));
		canvas.points.addElement(new Geopoint(0, 55d));
		canvas.points.addElement(new Geopoint(-66.5622d, 34.45d));
		canvas.points.addElement(new Geopoint(-85, 15d));
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

	public static void BringMenu() {
		display.setCurrent(menu);
	}

	public static MapCanvas GetCanvas() {
		return canvas;
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

	public static boolean IsKemulator() {
		try {
			Class.forName("emulator.Emulator");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

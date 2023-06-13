package mahomaps;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;

import mahomaps.api.YmapsApi;
import mahomaps.map.TilesProvider;
import mahomaps.overlays.OverlaysManager;
import mahomaps.route.RouteTracker;
import mahomaps.screens.MapCanvas;
import mahomaps.screens.MenuScreen;
import mahomaps.screens.SearchScreen;
import mahomaps.screens.Splash;

public class MahoMapsApp extends MIDlet implements Runnable, CommandListener {

	// globals
	public static Display display;
	public static Thread thread;
	public static TilesProvider tiles;
	private static MapCanvas canvas;
	private static MenuScreen menu;
	private static MahoMapsApp midlet;
	public static SearchScreen lastSearch;
	public static RouteTracker route;
	public static final YmapsApi api = new YmapsApi();

	// locale
	public static String[] text;

	// info cache
	public static final String platform = System.getProperty("microedition.platform");
	public static boolean bb = platform.toLowerCase().indexOf("blackberry") != -1;
	public static String version;
	public static String commit;
	public static boolean paused;
	private ChoiceGroup lang, bbNet;

	// commands
	public static Command exit;
	public static Command back;
	public static Command ok;
	public static Command rms;
	public static Command openLink;
	public static Command reset;
	public static Command no;
	public static Command yes;
	public static Command toMap;

	public MahoMapsApp() {
		display = Display.getDisplay(this);
	}

	protected void startApp() {
		paused = false;
		if (thread == null) {
			version = getAppProperty("MIDlet-Version");
			commit = getAppProperty("Commit");
			midlet = this;
			Settings.Read();
			if (Settings.firstLaunch) {
				processFirstLaunch();
				return;
			}
			thread = new Thread(this, "Init & repaint thread");
			thread.start();
		}
	}

	private void processFirstLaunch() {
		if (lang != null)
			return;

		// do not translate anything here
		Form f = new Form("Setup");
		lang = new ChoiceGroup("Language", Choice.EXCLUSIVE, new String[] { "Russian", "English", "French" }, null);
		bbNet = new ChoiceGroup("Network", Choice.EXCLUSIVE, new String[] { "Cellular", "Wi-Fi" }, null);
		f.append(lang);
		if (bb) {
			f.append(bbNet);
		}
		f.addCommand(ok);
		f.setCommandListener(this);
		BringSubScreen(f);
		return;
	}

	protected void destroyApp(boolean arg0) {
		if (thread != null)
			thread.interrupt();
		if (tiles != null)
			tiles.Stop();
		if (canvas != null)
			canvas.dispose();
	}

	protected void pauseApp() {
		paused = true;
	}

	public void run() {
		try {
			BringSubScreen(new Splash());
		} catch (Throwable t) {
			// just in case
		}
		try {
			tiles = new TilesProvider(Settings.GetLangString()); // wrong lang in settings
		} catch (RuntimeException e) {
			Form f = new Form(MahoMapsApp.text[88],
					new Item[] { new StringItem(MahoMapsApp.text[91], MahoMapsApp.text[92]) });
			f.addCommand(exit);
			f.setCommandListener(this);
			BringSubScreen(f);
			thread = null;
			return;
		}
		if (!TryInitFSCache(true)) { // catch(Throwable) inside
			thread = null;
			return;
		}
		api.TryRead(); // catch(Throwable) inside
		try {
			if (api.token == null)
				api.RefreshToken();
		} catch (Throwable e) {
			// network or OOM errors
		}
		try {
			menu = new MenuScreen(tiles); // nothing to fail
			canvas = new MapCanvas(tiles); // hz
			tiles.Start(); // OOM
			BringMap(); // jic
		} catch (Throwable t) {
			Form f = new Form(MahoMapsApp.text[88], new Item[] { new StringItem("Инициализация", t.toString()) });
			f.addCommand(exit);
			f.setCommandListener(this);
			BringSubScreen(f);
			thread = null;
			return;
		}
		try {
			(new UpdateCheckThread()).start();
		} catch (Throwable t) {
			// OOM
		}
		try {
			canvas.run();
		} catch (InterruptedException e) {
		} catch (Throwable t) {
			Form f = new Form(MahoMapsApp.text[88], new Item[] { new StringItem("Поток отрисовки", t.toString()) });
			f.addCommand(exit);
			f.setCommandListener(this);
			BringSubScreen(f);
		}
		thread = null;
	}

	public static String getAppropCachePath() {
		String loc = null;
		if (IsKemulator()) {
			loc = "file:///root/ym/";
		} else if (IsJ2MEL()) {
			loc = "file:///C:/MahoMaps/";
		} else {
			loc = fixPath(System.getProperty("fileconn.dir.private"), null);
			if (loc == null)
				loc = fixPath(System.getProperty("fileconn.dir.photos"), "MahoMaps/");
			if (loc == null)
				loc = "file:///C:/MahoMaps/";
		}
		return loc;
	}

	private static String fixPath(String loc, String suf) {
		if (loc == null)
			return null;
		if (loc.charAt(loc.length() - 1) != '/')
			loc = loc + "/";
		if (suf != null)
			loc = loc + suf;
		return loc;
	}

	/**
	 * Запускает кэш с файловой.
	 *
	 * @param allowSwitch True, если предоставляется возможность использовать RMS.
	 *                    Это перезапустит приложение.
	 * @return False, если инициализировать не удалось.
	 */
	public static boolean TryInitFSCache(boolean allowSwitch) {
		try {
			if (Settings.cacheMode == Settings.CACHE_FS)
				tiles.InitFSCache(getAppropCachePath());
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			Form f = new Form(MahoMapsApp.text[88],
					new Item[] {
							new StringItem(MahoMapsApp.text[89], MahoMapsApp.text[90] + " " + getAppropCachePath()),
							new StringItem(e.getClass().getName(), e.getMessage()) });
			f.addCommand(exit);
			if (allowSwitch)
				f.addCommand(rms);
			f.setCommandListener(midlet);
			BringSubScreen(f);
			return false;
		}
	}

	public static void BringSubScreen(Displayable screen) {
		display.setCurrent(screen);
	}

	public static void BringSubScreen(Alert a, Displayable screen) {
		display.setCurrent(a, screen);
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

	public static OverlaysManager Overlays() {
		return canvas.overlays;
	}

	public static void open(String link) {
		try {
			midlet.platformRequest(link);
		} catch (ConnectionNotFoundException e) {
		}
	}

	public static void Exit() {
		midlet.destroyApp(true);
		midlet.notifyDestroyed();
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
			Class.forName("emulator.custom.CustomMethod");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean IsJ2MEL() {
		try {
			Class.forName("javax.microedition.shell.MicroActivity");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// called in settings load
	public static void LoadLocale(String name) {
		text = splitFull(getStringFromJAR("/" + name + ".txt"), '\n');
		if (text == null)
			throw new RuntimeException("Lang is not loaded");
		if (text.length != 122)
			throw new RuntimeException("Lang is outdated");
		for (int i = 0; i < text.length; i++) {
			if (text[i].endsWith("\r")) {
				text[i] = text[i].substring(0, text[i].length() - 1);
			}
		}
		exit = new Command(text[0], Command.EXIT, 0);
		back = new Command(text[1], Command.BACK, 1);
		ok = new Command(text[2], Command.OK, 0);
		rms = new Command(text[3], Command.OK, 0);
		openLink = new Command(text[4], Command.ITEM, 0);
		reset = new Command(text[5], Command.ITEM, 0);
		no = new Command(text[6], Command.CANCEL, 1);
		yes = new Command(text[7], Command.OK, 0);
		toMap = new Command(text[8], Command.SCREEN, 0);
	}

	public static double pow(double a, double b) {
		boolean gt1 = (Math.sqrt((a - 1) * (a - 1)) <= 1) ? false : true;
		int oc = -1, iter = 30;
		double p = a, x, x2, sumX, sumY;

		if ((b - Math.floor(b)) == 0) {
			for (int i = 1; i < b; i++)
				p *= a;
			return p;
		}

		x = (gt1) ? (a / (a - 1)) : (a - 1);
		sumX = (gt1) ? (1 / x) : x;

		for (int i = 2; i < iter; i++) {
			p = x;
			for (int j = 1; j < i; j++)
				p *= x;

			double xTemp = (gt1) ? (1 / (i * p)) : (p / i);

			sumX = (gt1) ? (sumX + xTemp) : (sumX + (xTemp * oc));

			oc *= -1;
		}

		x2 = b * sumX;
		sumY = 1 + x2;

		for (int i = 2; i <= iter; i++) {
			p = x2;
			for (int j = 1; j < i; j++)
				p *= x2;

			int yTemp = 2;
			for (int j = i; j > 2; j--)
				yTemp *= j;

			sumY += p / yTemp;
		}

		return sumY;
	}

	/**
	 *
	 * @param a Cos value [-1; 1]
	 * @return Angle in radians.
	 */
	public static double acos(double a) {
		final double epsilon = 1.0E-14;
		double x = a;
		do {
			x -= (Math.sin(x) - a) / Math.cos(x);
		} while (Math.abs(Math.sin(x) - a) > epsilon);

		// returned angle is in radians
		return -1 * (x - Math.PI / 2);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == exit) {
			Exit();
		} else if (c == rms) {
			Settings.cacheMode = Settings.CACHE_RMS;
			Settings.Save();
			startApp();
		} else if (c == ok) {
			if (thread != null)
				throw new IllegalStateException("First-run setup can't be finished if app is running!");
			int selLang = lang.getSelectedIndex();
			Settings.uiLang = selLang;
			if (selLang == 1 || selLang == 2) {
				// english api for english/french UI
				Settings.apiLang = 1;
			}
			if (bb)
				Settings.bbWifi = bbNet.getSelectedIndex() == 1;
			Settings.firstLaunch = false;
			Settings.Save();
			bbNet = null;
			lang = null;
			startApp();
		}
	}

	public static String getConnectionParams() {
		if (!bb || !Settings.bbWifi) {
			return "";
		}
		return ";deviceside=true;interface=wifi";
	}

	public static final String getStringFromJAR(String path) {
		try {
			StringBuffer sb = new StringBuffer();
			char[] chars = new char[1024];
			InputStream stream = MahoMapsApp.class.getResourceAsStream(path);
			if (stream == null)
				return null;
			InputStreamReader isr;
			isr = new InputStreamReader(stream, "UTF-8");
			while (true) {
				int c = isr.read(chars);
				if (c == -1)
					break;
				sb.append(chars, 0, c);
			}
			isr.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String[] splitFull(String str, char c) {
		if (str == null)
			return null;
		Vector v = new Vector(64, 16);
		int lle = 0;
		while (true) {
			int nle = str.indexOf(c, lle);
			if (nle == -1) {
				v.addElement(str.substring(lle, str.length()));
				break;
			}

			v.addElement(str.substring(lle, nle));
			lle = nle + 1;
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}
}

package mahomaps.map;

import java.util.Vector;

import mahomaps.screens.MapCanvas;

public class GeoUpdateThread extends Thread {

	/**
	 * Точка геопозиции.
	 */
	final Geopoint positionPoint;
	/**
	 * Состояние получения геопозиции. Одно из state-значений.
	 */
	public int state;
	private ILocationAPI locationAPI;
	public boolean loop = true;
	public long lastUpdateTime = System.currentTimeMillis();
	public String method = null;
	public int sattelites = -1;
	public int totalSattelitesInView = -1;
	public int updateCount;
	private Object lock = new Object();
	public boolean vibrated;

	public GeoUpdateThread(Geopoint positionPoint, MapCanvas map) {
		super("Geo update");
		this.positionPoint = positionPoint;
	}

	public void run() {
		try {
			Class.forName("javax.microedition.location.LocationProvider");
			try {
				locationAPI = new LocationAPI();
				locationAPI.setThread(this);
			} catch (Exception e) {
				state = e.toString().indexOf("LocationException") != -1 ? STATE_UNAVAILABLE : STATE_UNSUPPORTED;
				e.printStackTrace();
			}
		} catch (Throwable e) {
			state = STATE_UNSUPPORTED;
			System.out.println("Location api is not supported");
			e.printStackTrace();
			return;
		}
		if (locationAPI == null) {
			try {
				while (true) {
					synchronized (lock) {
						lock.wait();
					}
					run();
				}
			} catch (Exception e) {
			}
			return;
		}
		positionPoint.lat = 0;
		positionPoint.lon = 0;
		state = STATE_PENDING;
		double[] coordinates = locationAPI.getLastKnownCoordinates();
		if (coordinates != null && coordinates[0] != 0 && coordinates[1] != 0) {
			positionPoint.lat = coordinates[0];
			positionPoint.lon = coordinates[1];
			positionPoint.color = Geopoint.COLOR_GRAY;
			state = STATE_OK_PENDING;
			lastUpdateTime = System.currentTimeMillis();
		}
		locationAPI.setupListener();
		try {
			while (true) {
				synchronized (lock) {
					lock.wait();
				}
				locationAPI.resetProvider();
			}
		} catch (Exception e) {
		}
	}

	public void restart() {
		synchronized (lock) {
			lock.notify();
		}
	}

	static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if (i == -1)
			return new String[] { str };
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while (i != -1) {
			str = str.substring(i + 1);
			if ((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}

	public void Dispose() {
		loop = false;
		interrupt();
	}

	/**
	 * Рисовать точку?
	 */
	public boolean DrawPoint() {
		if (positionPoint.lat == 0 && positionPoint.lon == 0)
			return false;
		if (state == STATE_OK_PENDING || state == STATE_OK || state == STATE_ERROR)
			return true;
		return false;
	}

	/**
	 * Гео не поддерживается устройством.
	 */
	public final static int STATE_UNSUPPORTED = 5;
	/**
	 * К гео запрещён доступ, либо оно отключено.
	 */
	public final static int STATE_UNAVAILABLE = 4;
	/**
	 * Геопозиция доступна, однако получить её не удалось.
	 */
	public final static int STATE_ERROR = 3;
	/**
	 * Геопозиция определяется, но ещё не известна.
	 */
	public final static int STATE_PENDING = 0;
	/**
	 * Геопозиция уже примерно известна, но ещё определяется.
	 */
	public final static int STATE_OK_PENDING = 1;
	/**
	 * Геопозиция известна.
	 */
	public final static int STATE_OK = 2;

	public final static int[] states = new int[] { 93, 93, 94, 88, 95, 96 };
}

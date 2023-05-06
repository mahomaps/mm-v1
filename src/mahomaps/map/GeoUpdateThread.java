package mahomaps.map;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

import mahomaps.screens.MapCanvas;

public class GeoUpdateThread extends Thread {

	/**
	 * Точка геопозиции.
	 */
	private final Geopoint positionPoint;
	private final MapCanvas map;
	/**
	 * Состояние получения геопозиции. Одно из state-значений.
	 */
	public int state;
	private LocationAPI locationAPI;
	private Object requestLock = new Object();
	public boolean waiting;

	public GeoUpdateThread(Geopoint positionPoint, MapCanvas map) {
		super("Geo update");
		this.positionPoint = positionPoint;
		this.map = map;
		try {
			Class.forName("javax.microedition.location.LocationProvider");
			try {
				locationAPI = new LocationAPI();
			} catch (Exception e) {
				state = STATE_UNSUPPORTED;
			}
		} catch (Throwable e) {
			state = STATE_UNSUPPORTED;
			System.out.println("Location api is not supported");
			e.printStackTrace();
		}
	}

	public void run() {
		if (locationAPI == null) {
			return;
		}
		state = STATE_PENDING;
		double[] coordinates = locationAPI.getLastKnownCoordinates();
		if (coordinates != null && coordinates[0] != 0 && coordinates[1] != 0) {
			positionPoint.lat = coordinates[0];
			positionPoint.lon = coordinates[1];
			state = STATE_OK;
		}

		try {
			while (true) {
				waiting = false;
				try {
					// state = STATE_PENDING;
					coordinates = locationAPI.getNewCoordinates(-1);
					if (coordinates[0] != 0 && coordinates[1] != 0) {
						positionPoint.lat = coordinates[0];
						positionPoint.lon = coordinates[1];
						state = STATE_OK;
					} else {
						state = STATE_UNAVAILABLE;
					}
				} catch (Exception e) {
					// надо потом сделать отображение последней известной точки, а не этот костыль
					if (state != STATE_OK)
						state = STATE_ERROR;
					e.printStackTrace();
				}
				waiting = true;
				synchronized (requestLock) {
					requestLock.wait(30000);
				}
			}
		} catch (InterruptedException e) {
		}
	}

	public void request() {
		synchronized (requestLock) {
			requestLock.notify();
		}
	}

	/**
	 * Гео не поддерживается устройством.
	 */
	public final static int STATE_UNSUPPORTED = 4;
	/**
	 * К гео запрещён доступ, либо оно отключено.
	 */
	public final static int STATE_UNAVAILABLE = 3;
	/**
	 * Геопозиция доступна, однако получить её не удалось.
	 */
	public final static int STATE_ERROR = 2;
	/**
	 * Геопозиция определяется, но ещё не известна.
	 */
	public final static int STATE_PENDING = 0;
	/**
	 * Геопозиция известна.
	 */
	public final static int STATE_OK = 1;

	public final static String[] states = new String[] { "Ожидание сигнала", "Готово", "Ошибка", "Недоступно",
			"Не поддерживается" };

	// для безопасных вызовов
	class LocationAPI {
		public LocationProvider locationProvider;

		public LocationAPI() throws Exception {
			locationProvider = LocationProvider.getInstance(new Criteria());
		}

		public double[] getLastKnownCoordinates() {
			Location location = LocationProvider.getLastKnownLocation();
			if (location == null)
				return null;
			Coordinates coordinates = location.getQualifiedCoordinates();
			return new double[] { coordinates.getLatitude(), coordinates.getLongitude() };
		}

		public double[] getNewCoordinates(int timeout) throws Exception {
			Coordinates coordinates = locationProvider.getLocation(timeout).getQualifiedCoordinates();
			return new double[] { coordinates.getLatitude(), coordinates.getLongitude() };
		}

	}
}

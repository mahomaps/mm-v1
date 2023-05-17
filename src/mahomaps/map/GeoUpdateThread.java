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
	public boolean loop = true;

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
		positionPoint.lat = 0;
		positionPoint.lon = 0;
		state = STATE_PENDING;
		double[] coordinates = locationAPI.getLastKnownCoordinates();
		if (coordinates != null && coordinates[0] != 0 && coordinates[1] != 0) {
			positionPoint.lat = coordinates[0];
			positionPoint.lon = coordinates[1];
			positionPoint.color = Geopoint.COLOR_GRAY;
			state = STATE_APPROX;
		}

		try {
			while (loop) {
				try {
					coordinates = locationAPI.getNewCoordinates(-1);
					if (coordinates[0] != 0 && coordinates[1] != 0) {
						positionPoint.lat = coordinates[0];
						positionPoint.lon = coordinates[1];
						positionPoint.color = Geopoint.COLOR_RED;
						state = STATE_OK;
					} else {
						state = STATE_UNAVAILABLE;
					}
				} catch (Exception e) {
					positionPoint.color = Geopoint.COLOR_GRAY;
					state = STATE_ERROR;
					e.printStackTrace();
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
		state = STATE_UNAVAILABLE;
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
		if (state == STATE_APPROX || state == STATE_OK || state == STATE_ERROR)
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
	public final static int STATE_APPROX = 1;
	/**
	 * Геопозиция известна.
	 */
	public final static int STATE_OK = 2;

	public final static String[] states = new String[] { "Ожидание сигнала", "Примерная", "Готово", "Ошибка",
			"Недоступно", "Не поддерживается" };

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

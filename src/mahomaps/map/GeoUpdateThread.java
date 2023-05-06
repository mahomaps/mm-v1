package mahomaps.map;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

import mahomaps.screens.MapCanvas;

public class GeoUpdateThread extends Thread {

	private final Geopoint positionPoint; // сюда проставлять координаты
	private final MapCanvas map; // отсюда можно дёргать состояние карты если надо
	public int state; // сюда проставлять статус, точка будет рисоваться только если =2
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
		if(locationAPI == null) {
			return;
		}
		// запустится по первому нажатию кнопки геолокации и будет крутиться пока не вырубишь прогу
		state = STATE_PENDING;
		double[] coordinates = locationAPI.getLastKnownCoordinates();
		if(coordinates != null && coordinates[0] != 0 && coordinates[1] != 0) {
			positionPoint.lat = coordinates[0];
			positionPoint.lon = coordinates[1];
			state = STATE_OK;
		}
		try {
			while (true) {
				waiting = false;
				try {
					coordinates = locationAPI.getNewCoordinates(20);
					if(coordinates[0] != 0 && coordinates[1] != 0) {
						positionPoint.lat = coordinates[0];
						positionPoint.lon = coordinates[1];
						state = STATE_OK;
						map.updateGeo();
					} else {
						state = STATE_UNAVAILABLE;
					}
				} catch (Exception e) {
					// надо потом сделать отображение последней известной точки, а не этот костыль
					if(state != STATE_OK) state = STATE_ERROR;
					e.printStackTrace();
				}
				waiting = true;
				synchronized(requestLock) {
					requestLock.wait();
				}
			}
		} catch(InterruptedException e) {
		}
	}

	public void request() {
		synchronized(requestLock) {
			requestLock.notify();
		}
	}

	public final static int STATE_UNSUPPORTED = -3;
	public final static int STATE_UNAVAILABLE = -2;
	public final static int STATE_ERROR = -1;
	public final static int STATE_PENDING = 1;
	public final static int STATE_OK = 2;
	
	// для безопасных вызовов
	class LocationAPI {
		public LocationProvider locationProvider;
		 
		public LocationAPI() throws Exception {
			locationProvider = LocationProvider.getInstance(new Criteria());
		}
		
		public double[] getLastKnownCoordinates() {
			Location location = LocationProvider.getLastKnownLocation();
			if(location == null) return null;
			Coordinates coordinates = location.getQualifiedCoordinates();
			return new double[] { coordinates.getLatitude(), coordinates.getLongitude() };
		}
		
		public double[] getNewCoordinates(int timeout) throws Exception {
			Coordinates coordinates = locationProvider.getLocation(timeout).getQualifiedCoordinates();
			return new double[] { coordinates.getLatitude(), coordinates.getLongitude() };
		}
		
	}
}

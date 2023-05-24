package mahomaps.map;

import java.util.Vector;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import mahomaps.MahoMapsApp;
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
	private LocationAPI locationAPI;
	public boolean loop = true;
	public long lastUpdateTime = System.currentTimeMillis();
	public String method = null;
	public int sattelites = -1;
	public int totalSattelitesInView = -1;
	private Object lock = new Object();

	public GeoUpdateThread(Geopoint positionPoint, MapCanvas map) {
		super("Geo update");
		this.positionPoint = positionPoint;
	}

	public void run() {
		try {
			Class.forName("javax.microedition.location.LocationProvider");
			try {
				locationAPI = new LocationAPI();
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
				while(true) {
					synchronized(lock) {
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
			while(true) {
				synchronized(lock) {
					lock.wait();
				}
				locationAPI.resetProvider();
			}
		} catch (Exception e) {
		}
	}
	
	public void restart() {
		synchronized(lock) {
			lock.notify();
		}
	}
	
	static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
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

	public final static String[] states = new String[] { "Ожид. сигнала", "Ожид. сигнала", "Готово", "Ошибка",
			"Недоступно", "Не поддерживается" };

	// для безопасных вызовов
	class LocationAPI {
		public LocationProvider locationProvider;

		public LocationAPI() throws Exception {
			locationProvider = LocationProvider.getInstance(null);
		}

		public double[] getLastKnownCoordinates() {
			Location location = LocationProvider.getLastKnownLocation();
			if (location == null || !location.isValid())
				return null;
			Coordinates coordinates = location.getQualifiedCoordinates();
			if (coordinates == null)
				return null;
			return new double[] { coordinates.getLatitude(), coordinates.getLongitude() };
		}

		public void setupListener() {
			locationProvider.setLocationListener(new LocationAPIListener(), 5, 5, 5);
		}

		public void resetProvider() throws Exception {
			System.out.println("resetProvider");
			Thread.sleep(5000);
			LocationProvider old = locationProvider;
			try {
				locationProvider = LocationProvider.getInstance(null);
				old.setLocationListener(null, 0, 0, 0);
				setupListener();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		class LocationAPIListener implements LocationListener {

			public void locationUpdated(LocationProvider provider, Location location) {
				// определение кол-ва спутников
				String nmea = location.getExtraInfo("application/X-jsr179-location-nmea");
				if(nmea != null) {
					String[] sequence = split(nmea, '$');
					int s1 = -1;
					int s2 = -1;
					for(int i = sequence.length-1; i >= 0; i--) {
						String[] sentence = split(sequence[i], ',');
						if(sentence[0].endsWith("GGA")) {
							try {
								s1 = Integer.parseInt(sentence[7]);
							} catch (Exception e) {
								s1 = -1;
							}
							s2 = Math.max(s2, s1);
						} else if(sentence[0].endsWith("GSV")) {
							try {
								s2 = Math.max(s2, Integer.parseInt(sentence[3]));
							} catch (Exception e) {
							}
						}
					}
					sattelites = s1;
					totalSattelitesInView = s2;
				} else {
					totalSattelitesInView = sattelites = -1;
				}
				String s = "";
				int t = location.getLocationMethod();
				if((t & Location.MTE_SATELLITE) == Location.MTE_SATELLITE) {
					s = "GPS";
				}
				if((t & Location.MTE_TIMEDIFFERENCE) == Location.MTE_TIMEDIFFERENCE) {
					s += "TD";
				}
				if((t & Location.MTE_TIMEOFARRIVAL) == Location.MTE_TIMEOFARRIVAL) {
					s += "TOA";
				}
				if((t & Location.MTE_CELLID) == Location.MTE_CELLID) {
					s += "CID";
				}
				if((t & Location.MTE_SHORTRANGE) == Location.MTE_SHORTRANGE) {
					s += "SR";
				}
				if((t & Location.MTE_ANGLEOFARRIVAL) == Location.MTE_ANGLEOFARRIVAL) {
					s += "AOA";
				}
				if((t & Location.MTA_ASSISTED) == Location.MTA_ASSISTED) {
					s = "A" + s;
				} else if((t & Location.MTA_UNASSISTED) == Location.MTA_UNASSISTED) {
					s = "U" + s;
				}
				if((t & Location.MTY_TERMINALBASED) == Location.MTY_TERMINALBASED) {
					s = "TB " + s;
				}
				if((t & Location.MTY_NETWORKBASED) == Location.MTY_NETWORKBASED) {
					s = "NB " + s;
				}
				method = s.length() == 0 ? null : s;
				if(location.isValid()) {
					Coordinates coordinates = location.getQualifiedCoordinates();
					if (coordinates.getLatitude() != 0 && coordinates.getLongitude() != 0) {
						positionPoint.lat = coordinates.getLatitude();
						positionPoint.lon = coordinates.getLongitude();
						positionPoint.color = Geopoint.COLOR_RED;
						state = STATE_OK;
						lastUpdateTime = System.currentTimeMillis();
						MahoMapsApp.GetCanvas().requestRepaint();
					} else {
						state = STATE_UNAVAILABLE;
					}
				}
			}

			public void providerStateChanged(LocationProvider provider, int newState) {
				if(newState != LocationProvider.AVAILABLE) {
					state = STATE_OK_PENDING;
				}
				// на случай если изменился провайдер
				if(newState == LocationProvider.OUT_OF_SERVICE) {
					restart();
				}
			}
		}
	}
}

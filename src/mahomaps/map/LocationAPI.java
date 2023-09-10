package mahomaps.map;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import mahomaps.MahoMapsApp;

// для безопасных вызовов
class LocationAPI implements ILocationAPI {

	/**
	 * Geo thread, where this api will work.
	 */
	private GeoUpdateThread geoUpdateThread;
	public LocationProvider locationProvider;

	public LocationAPI() throws Exception {
		locationProvider = LocationProvider.getInstance(null);
	}

	public void setThread(GeoUpdateThread geoUpdateThread) {
		this.geoUpdateThread = geoUpdateThread;
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
			if (nmea != null) {
				String[] sequence = GeoUpdateThread.split(nmea, '$');
				int s1 = -1;
				int s2 = -1;
				for (int i = sequence.length - 1; i >= 0; i--) {
					String[] sentence = GeoUpdateThread.split(sequence[i], ',');
					if (sentence[0].endsWith("GGA")) {
						try {
							s1 = Integer.parseInt(sentence[7]);
						} catch (Exception e) {
							s1 = -1;
						}
						s2 = Math.max(s2, s1);
					} else if (sentence[0].endsWith("GSV")) {
						try {
							s2 = Math.max(s2, Integer.parseInt(sentence[3]));
						} catch (Exception e) {
						}
					}
				}
				geoUpdateThread.sattelites = s1;
				geoUpdateThread.totalSattelitesInView = s2;
			} else {
				geoUpdateThread.totalSattelitesInView = geoUpdateThread.sattelites = -1;
			}
			String s = "";
			int t = location.getLocationMethod();
			if ((t & Location.MTE_SATELLITE) == Location.MTE_SATELLITE) {
				s = "GPS";
			}
			if ((t & Location.MTE_TIMEDIFFERENCE) == Location.MTE_TIMEDIFFERENCE) {
				s += "TD";
			}
			if ((t & Location.MTE_TIMEOFARRIVAL) == Location.MTE_TIMEOFARRIVAL) {
				s += "TOA";
			}
			if ((t & Location.MTE_CELLID) == Location.MTE_CELLID) {
				s += "CID";
			}
			if ((t & Location.MTE_SHORTRANGE) == Location.MTE_SHORTRANGE) {
				s += "SR";
			}
			if ((t & Location.MTE_ANGLEOFARRIVAL) == Location.MTE_ANGLEOFARRIVAL) {
				s += "AOA";
			}
			if ((t & Location.MTA_ASSISTED) == Location.MTA_ASSISTED) {
				s = "A" + s;
			} else if ((t & Location.MTA_UNASSISTED) == Location.MTA_UNASSISTED) {
				s = "U" + s;
			}
			if ((t & Location.MTY_TERMINALBASED) == Location.MTY_TERMINALBASED) {
				s = "TB " + s;
			}
			if ((t & Location.MTY_NETWORKBASED) == Location.MTY_NETWORKBASED) {
				s = "NB " + s;
			}
			geoUpdateThread.method = s.length() == 0 ? null : s;
			if (location.isValid()) {
				Coordinates coordinates = location.getQualifiedCoordinates();
				if (coordinates.getLatitude() != 0 && coordinates.getLongitude() != 0) {
					if (!geoUpdateThread.vibrated) {
						try {
							MahoMapsApp.display.vibrate(100);
						} catch (Exception e) {
						}
						geoUpdateThread.vibrated = true;
					}
					geoUpdateThread.positionPoint.lat = coordinates.getLatitude();
					geoUpdateThread.positionPoint.lon = coordinates.getLongitude();
					geoUpdateThread.positionPoint.color = Geopoint.COLOR_RED;
					geoUpdateThread.state = GeoUpdateThread.STATE_OK;
					geoUpdateThread.lastUpdateTime = System.currentTimeMillis();
					MahoMapsApp.GetCanvas().requestRepaint();
				} else {
					geoUpdateThread.state = GeoUpdateThread.STATE_UNAVAILABLE;
				}
			}
			geoUpdateThread.updateCount++;
		}

		public void providerStateChanged(LocationProvider provider, int newState) {
			if (newState != LocationProvider.AVAILABLE) {
				geoUpdateThread.state = GeoUpdateThread.STATE_OK_PENDING;
			}
			// на случай если изменился провайдер
			if (newState == LocationProvider.OUT_OF_SERVICE) {
				geoUpdateThread.restart();
			}
		}
	}
}
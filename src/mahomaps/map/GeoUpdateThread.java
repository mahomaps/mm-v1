package mahomaps.map;

import mahomaps.screens.MapCanvas;

public class GeoUpdateThread extends Thread {

	private final Geopoint positionPoint; // сюда проставлять координаты
	private final MapCanvas map; // отсюда можно дёргать состояние карты если надо
	public int state; // сюда проставлять статус, точка будет рисоваться только если =2

	public GeoUpdateThread(Geopoint positionPoint, MapCanvas map) {
		super("Geo update");
		this.positionPoint = positionPoint;
		this.map = map;
	}

	public void run() {
		// запустится по первому нажатию кнопки геолокации и будет крутиться пока не вырубишь прогу
		state = STATE_PENDING;
		while (true) {
			// TODO срать геопозицию
			positionPoint.lat = 69.292292;
			positionPoint.lon = 42.727727;
			state = STATE_OK;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// dispose();
				// state = STATE_PENDING;
				return;
			}
		}
	}

	public final static int STATE_PENDING = 0;
	public final static int STATE_ERROR = 1;
	public final static int STATE_OK = 2;
}

package mahomaps.route;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import mahomaps.map.Geopoint;

public abstract class RouteSegment {

	protected RouteSegment(int sv) {
		segmentStartVertex = sv;
	}

	/**
	 * Index of line's vertex, where this segment starts.
	 */
	public final int segmentStartVertex;

	/**
	 * @return May return a geopoint to show it as maneuver on map.
	 */
	public Geopoint GetAnchor() {
		return null;
	}

	/**
	 * @return Дистанция сегмента в метрах.
	 */
	public abstract int GetDistance();

	public abstract int GetIcon();

	public abstract String GetType();

	/**
	 * Описание сегмента. Может быть null.
	 *
	 * @return Описание сегмента.
	 */
	public abstract String GetDescription();

	/**
	 * Получает один элемент LCDUI, которым можно отобразить сегмент. Этот метод не
	 * вызывается напрямую, его использует {@link #ToLcdui()}, не переопределяйте
	 * его.
	 *
	 * @return Элемент, представляющий сегмент.
	 */
	public Item ToLcduiSingle() {
		return new StringItem(GetType(), GetDescription());
	}

	/**
	 * Возвращает элементы, отображающие сегмент. Если не переопределён, возвращает
	 * 1 элемент возвращённый методом {@link #ToLcduiSingle()}.
	 *
	 * @return Элементы, представляющий сегмент.
	 */
	public Item[] ToLcdui() {
		return new Item[] { ToLcduiSingle() };
	}

	public static final int MANEUVER_ANGLE = -1;
	public static final int NO_ICON = 0;
	public static final int ICON_WALK = 1;
	public static final int ICON_BUS = 2;
	public static final int ICON_METRO = 3;
	public static final int ICON_SUBURBAN = 4;

}

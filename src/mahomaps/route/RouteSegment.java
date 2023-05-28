package mahomaps.route;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import mahomaps.map.Geopoint;

public abstract class RouteSegment {

	public Geopoint GetAnchor() {
		return null;
	}

	/**
	 * @return Дистанция сегмента в метрах. Для действий верните 0.
	 */
	public abstract int GetDistance();

	public abstract String GetIcon();

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

}

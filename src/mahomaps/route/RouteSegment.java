package mahomaps.route;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

public abstract class RouteSegment {
	/**
	 * @return Дистанция сегмента в метрах. Для действий верните 0.
	 */
	public abstract int GetDistance();

	/**
	 * Получает дистанцию в читабельном виде. Переопределите для изменения формата
	 * вывода.
	 *
	 * @return Дистанция сегмента. Не null.
	 */
	public String GetDistanceString() {
		return GetDistance() + "м";
	}

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
		String text = GetDescription();
		if (GetDistance() != 0) {
			if (text == null)
				text = GetDistanceString();
			else
				text = text + "\n" + GetDistanceString();
		}
		return new StringItem(GetType(), text);
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

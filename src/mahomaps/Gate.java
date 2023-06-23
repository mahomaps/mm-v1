package mahomaps;

public class Gate {
	private volatile boolean pass;
фифщиф
	private final Object lock = new Object();

	/**
	 * Создаёт гейт.
	 *
	 * @param initState True, если 1 ближайший проход потока должен быть разрешён.
	 */
	public Gate(boolean initState) {
		pass = initState;
	}

	/**
	 * Проходит гейт. Если прохождение было разрешено вызовом {@link #Reset()},
	 * метод возвращается. Если гейт заблокирован, ожидает ближайшего вызова
	 * {@link #Reset()}.
	 *
	 * @throws InterruptedException Ожидание прервано.
	 */
	public void Pass() throws InterruptedException {
		synchronized (lock) {
			if (!pass) {
				lock.wait();
			}
			pass = false;
		}
	}

	/**
	 * Проходит гейт. Если прохождение было разрешено вызовом {@link #Reset()},
	 * метод возвращается. Если гейт заблокирован, ожидает ближайшего вызова
	 * {@link #Reset()} до таймаута.
	 *
	 * @throws InterruptedException Ожидание прервано.
	 */
	public void Pass(int timeout) throws InterruptedException {
		synchronized (lock) {
			if (!pass) {
				lock.wait(timeout);
			}
			pass = false;
		}
	}

	/**
	 * Запрещает потоку прохождение через гейт, если оно было разрешено вызовом
	 * {@link #Reset()}.
	 */
	public void Set() {
		synchronized (lock) {
			pass = false;
		}
	}

	/**
	 * Разрешает ожидающему потоку пройти гейт. Если поток ещё не достиг гейта, ему
	 * будет разрешено пройти 1 раз в будущем. После прохождения потока гейт будет
	 * заблокирован до следующего вызова {@link #Reset()}.
	 */
	public void Reset() {
		synchronized (lock) {
			pass = true;
			lock.notify();
		}
	}
}

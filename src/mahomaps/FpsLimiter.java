package mahomaps;

public final class FpsLimiter {
	private final Gate gate = new Gate(true);
	private long time;

	public void Reset() {
		gate.Reset();
	}

	public void Begin() {
		time = System.currentTimeMillis();
	}

	public void End(int timeout) throws InterruptedException {
		int delay = timeout - (int) (System.currentTimeMillis() - time);
		if (delay <= 0)
			return;

		gate.Pass(delay);
	}

	public void Pass() throws InterruptedException {
		gate.Pass();
	}
}
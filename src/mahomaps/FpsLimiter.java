package mahomaps;

public final class FpsLimiter extends Gate {

	private long time;
	
	public FpsLimiter() {
		super(true);
	}

	public void Begin() {
		time = System.currentTimeMillis();
	}

	public void End(int timeout) throws InterruptedException {
		int delay = timeout - (int) (System.currentTimeMillis() - time);
		if (delay <= 0)
			return;

		super.Pass(delay);
	}
}
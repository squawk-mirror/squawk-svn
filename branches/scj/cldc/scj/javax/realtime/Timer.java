package javax.realtime;

public abstract class Timer extends AsyncEvent {

	protected Timer(HighResolutionTime time, Clock cclock,
			AsyncEventHandler handler) {
	}

	public boolean isRunning() {
		return false;
	}

	public void start() {
	}

	public void start(boolean disabled) {
	}

	public boolean stop() {
		return false;
	}

	public ReleaseParameters createReleaseParameters() {
		return null;
	}

	public void enable() {
	}

	public void disable() {
	}

	public void destroy() {
	}

	public void fire() {
	}

	public Clock getClock() {
		return null;
	}

	public AbsoluteTime getFireTime() {
		return null;
	}

	public void reschedule(HighResolutionTime time) {
	}

	void fireIt() {
	}
}

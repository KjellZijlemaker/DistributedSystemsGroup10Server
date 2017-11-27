package distributed.systems.das.events;

import distributed.systems.das.util.Log;

public class Notify implements Runnable {

	private final long minInterval;

	private volatile Listener listener;
	private Thread thread = null;
	private long lastNotify;
	private boolean running = false;

	public Notify (long minInterval) {
		this.minInterval = minInterval;
	}

	public synchronized void subscribe (Listener listener) {
		this.listener = listener;
	}

	public synchronized void unsubscribe (Listener listener) {
		this.listener = null;
	}

	public synchronized void start () throws AlreadyRunningException {
		if (!this.running && this.thread == null) {
			this.thread = new Thread (this);
			this.lastNotify = System.currentTimeMillis ();
			this.running = true;
			thread.start ();
		} else {
			throw new AlreadyRunningException ();
		}
	}

	public synchronized void stop () {
		running = false;
	}

	@Override
	public void run () {
		while (running) {
			try {
				Thread.sleep (minInterval);
			} catch (InterruptedException e) {
				Log.throwException (e, this.getClass ());
				// TODO: recover from this?
			}
			updateTime ();
		}
	}

	private void updateTime () {
		long currentTime = this.lastNotify;
		this.lastNotify = System.currentTimeMillis ();

		if (listener != null) {
			synchronized (this) {
				if (listener != null) {
					listener.update (currentTime);
				}
			}
		}
	}

	public static class AlreadyRunningException extends Exception {

		AlreadyRunningException () {
			super ("Notify is already running!");
		}
	}

	public interface Listener {
		public void update (long time);
	}
}

package distributed.systems.das.events;

import distributed.systems.das.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class Notify implements Runnable {

	private final long minInterval;
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<> ();

	private Thread thread = null;
	private long lastNotify;
	private boolean running = false;

	public Notify (long minInterval) {
		this.minInterval = minInterval;
	}

	public void subscribe (Listener listener) {
		listeners.add (listener);
	}

	public void unsubscribe (Listener listener) {
		listeners.remove (listener);
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

		for (Listener listener : listeners) {
			listener.update (currentTime - lastNotify);
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

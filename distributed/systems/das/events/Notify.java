package distributed.systems.das.events;

import distributed.systems.das.util.Log;

public class Notify implements Runnable
{
	public interface Listener
	{
		public void update (long time);
	}

	private long lastNotify;
	private Listener listener;
	private boolean running = false;
	private Thread thread = null;
	private final long minInterval;

	public Notify(long minInterval)
	{
		this.minInterval = minInterval;
	}
	public synchronized boolean subscribe(Listener listener)
	{
		boolean clear = this.listener == null;
		this.listener = listener;
		return clear;
	}

	public synchronized void start () throws AlreadyRunningException
	{
		if(!this.running && this.thread == null)
		{
			this.thread = new Thread(this);
			this.lastNotify = System.currentTimeMillis ();
			this.running = true;
			thread.start();
		}
		else
		{
			throw new AlreadyRunningException ();
		}
	}

	public synchronized void stop()
	{
		running = false;
	}

	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				Thread.sleep (minInterval);
			}
			catch (InterruptedException e)
			{
				Log.throwException (e, this.getClass ());
			}
			updateTime ();
		}
	}

	private void updateTime () {
		long currentTime = this.lastNotify;
		this.lastNotify = System.currentTimeMillis ();

		Listener l = null;
		synchronized (this)
		{
			l = listener;
		}

		if (l != null)
		{
			l.update (currentTime);
		}
	}

	public static class AlreadyRunningException extends Exception {

		public AlreadyRunningException () {
			super ("Notify is already running!");
		}
	}
}

package distributed.systems.das.events;

public class Notify implements Runnable
{
	public class interface Listener
	{
		public void update(long interval);
	}

	private long lastnotify;
	private Listener listener;
	private boolean running = false;
	private Thread thread = null;
	private final long minInterval;
	private final long scale;

	public Notify(long minInterval)
	{
		this(minInterval, 100);
	}

	public Notify(long minInterval, long scale)
	{
		this.minInterval = minInterval;
		this.scale = scale;
	}

	public synchronized boolean subscribe(Listener listener)
	{
		boolean clear = this.listener == null;
		this.listener = listener;
		return clear;
	}

	public synchronized void start() throws NotifyRunningException
	{
		if(!this.running && this.thread == null)
		{
			this.thread = new Thread(this);
			this.lastnotify = System.currentTimeMillis();
			this.running = true;
			thread.start();
		}
		else
		{
			throw new NotifyRunningException;
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
				Thread.sleep(minInterval * scale);
			}
			catch (InterruptedException e)
			{
                Logger.getLogger(this.getClass().toString()).severe(e.getMessage());
			}
			onFire();
		}
	}

	private void onFire()
	{
		long t = this.lastfire;
		this.lastfire = System.currentTimeMillis();

		Listener l = null;
		synchronized (this)
		{
			l = listener;
		}

		if (l != null)
		{
			l.update((lastfire - t) / scale);
		}
	}

	public static class NotifyRunningException extends Exception
	{
		public NotifyRunningException(String message, Throwable inner)
		{
			super(message, inner);
		}

		public NotifyRunningException(String message)
		{
			super(message, null);
		}

		public NotifyRunningException()
		{
			super("Active notification.");
		}
	}
}

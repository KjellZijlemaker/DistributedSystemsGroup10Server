package distributed.systems.das.server.State;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Services.WishList;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.util.AlreadyRunningException;
import distributed.systems.das.server.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of the TSS algorithm. Use the TSSBuilder class to instantiate.
 */
public class TrailingStateSynchronization implements Notify.Listener, IMessageReceivedHandler,
													 Runnable {

	private CopyOnWriteArrayList<GameState> states = new CopyOnWriteArrayList<GameState> ();
	private CopyOnWriteArrayList<Integer> delayIntervals = new CopyOnWriteArrayList<> ();
	private EventList pendingEvents = new EventList ();
	private Notify notify;

	private boolean running = false;
	private Thread thread = null;

	/**
	 * @param startingState
	 * @param delayInterval interval between the states. Must be divisible by {@code tickrate}
	 * @param delays        number of delays
	 * @param tickrate      rate at which time is updated
	 * @param wishList        {@code WishList} object that will be subscribed to
	 */
	private TrailingStateSynchronization (GameState startingState, int delayInterval,
										  int delays, int tickrate, WishList wishList) {

		if (delayInterval % tickrate != 0) {
			throw new IllegalArgumentException ("TSS delayInterval MUST be divisible by " +
												"tickrate!");
		}

		subscribeToNotifications (tickrate, wishList);
		long time = startingState.getTime ();

		for (int i = 0; i < delays; ++i) { // create states
			GameState state = GameState.clone (startingState);
			state.updateTime (time - (i * delayInterval)); // create trails
			this.states.add (state);
			this.delayIntervals.add (delayInterval);
		}

		try {
			start ();
		} catch (AlreadyRunningException e) {
			Log.throwException (e, this.getClass ());
			e.printStackTrace ();
		}
	}

	private synchronized void subscribeToNotifications (int tickrate, WishList wishList) {
		this.notify = new Notify (tickrate);
		try {
			this.notify.start ();
		} catch (AlreadyRunningException e) {
			Log.throwException (e, this.getClass ());
			// TODO: Handle this. Can probably just ignore, since it's already running.
		}
		this.notify.subscribe (this);
		wishList.registerListener (this);
	}

	private synchronized void start () throws AlreadyRunningException {
		if (!this.running && this.thread == null) {
			this.thread = new Thread (this);
			this.running = true;
			thread.start ();
		} else {
			throw new AlreadyRunningException (this.getClass ());
		}
	}

	/**
	 * Adds Event to pending list of events
	 */
	public synchronized void addEvent (Event event) {
		for (int i = 0; i < states.size (); ++i) {
			GameState state = states.get (i);
			if (state.getTime () > event.getTimestamp ()) {
				// "Late moves whose timestamps are earlier than the current execution time for a
				// state are placed at the head of the pending list for the state and
				// are executed immediately"
				pendingEvents.add (0, event);
			} else {
				pendingEvents.add (event);
			}
		}
	}

	/**
	 * Executes top element of the pending list of events
	 * @return false if no more events left to execute
	 */
	public synchronized boolean executeEvent () {
		int i = 1;
		if (pendingEvents.isEmpty ()) {
			return false;
		}
		Event event = pendingEvents.pop ();
		GameState beforeState = GameState.clone (getState (0));
		GameState afterState = getState (0);

		// Execute the command in the leading game state
		afterState.execute (event);

		Notify.Listener listener = new EventActionListener (i, beforeState, afterState,
															event);
		this.notify.subscribe (listener);
		return true;
	}

	// TODO: We don't need the whole state, we only need to be able to detect inconsistencies
	public synchronized GameState getState (int index) {
		return this.states.get (index);
	}

	/**
	 * Returns whether the changes an event have made to two different states are the same
	 */
	public synchronized boolean compareStates (GameState x, GameState y) {
		return x == y;
	}

	@Override
	public void update (long time) {
		for (GameState state : states) {
			state.updateTime (time);
		}
		if (!running) {
			this.running = true;
			thread.start ();
		}
	}

	@Override
	public void onMessageReceived (Event event) {
		addEvent (event);
	}

	@Override
	public void run () {
		while (running) {
			running = executeEvent ();
		}
	}

	private class EventActionListener implements Notify.Listener {

		private final long tickRate;

		private int index;
		private GameState before;
		private GameState after;
		private long eventId;
		private Event event;

		private int counter;

		public EventActionListener (int index, GameState before,
									GameState after, Event event) {
			this.index = index;
			this.before = before;
			this.after = after;
			this.eventId = event.getId ();
			this.event = event;
			this.counter = 0;
			this.tickRate = notify.getTickRate ();
		}

		@Override
		public void update (long time) {
			++counter;
			if (tickRate / delayIntervals.get (index) == counter) {
				checkConsistency ();
			}
		}

		public void checkConsistency () {

			// TODO: might instead need to track event by its id, and not execute it here

			// "To detect inconsistencies, each trailing state looks at the changes in game
			// state that the execution of a command produced, and compares them with the
			// changes recorded in the directly preceding state"

			GameState comparisonAfter = getState (index);
			GameState comparisonBefore = GameState.clone (comparisonAfter);
			comparisonAfter.execute (event);
			if (!compareStates (this.before, comparisonBefore) ||
				!compareStates (this.after, comparisonAfter)) {
				long time = this.after.getTime ();

				// TODO: More efficient way of replacing state? Perhaps only update select vars
				// 		 That would also get rid of the useless updateTime() call
				boolean success = this.after.replace (comparisonAfter);

				if (!success) {
					Log.throwException (new Exception (), this.getClass ());
					// TODO: Handle error better than this
				}

				// Set the time to the actual time. synchronize() will then process all the
				// events that now missing from the state because of overwriting the event list
				// with the event list from the older state.
				this.after.setTime (time);
				this.after.synchronize ();
			}

			this.before = comparisonBefore;
			this.after = comparisonAfter;
			++this.index;
		}
	}

	public static class TSSBuilder {
		private GameState startingState;
		private int delayInterval;
		private int delays;
		private int tickrate;
		private WishList wishList;

		public TSSBuilder (GameState startingState) {
			this.startingState = startingState;
		}

		public int getDelayInterval () {
			return delayInterval;
		}

		public void setDelayInterval (int delayInterval) {
			this.delayInterval = delayInterval;
		}

		public int getDelays () {
			return delays;
		}

		public void setDelays (int delays) {
			this.delays = delays;
		}

		public int getTickrate () {
			return tickrate;
		}

		public void setTickrate (int tickrate) {
			this.tickrate = tickrate;
		}

		public WishList getWishList () {
			return wishList;
		}

		public void setWishList (WishList wishList) {
			this.wishList = wishList;
		}

		public TrailingStateSynchronization createTSS () {
			return new TrailingStateSynchronization (this.startingState, this.delayInterval,
													 this.delays, this.tickrate, this.wishList);
		}
	}

}

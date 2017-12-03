package distributed.systems.das.server.State;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Services.Wishlist;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;
import distributed.systems.das.server.util.AlreadyRunningException;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of the TSS algorithm. Use the TSSBuilder class to instantiate.
 */
public class TrailingStateSynchronization implements Notify.Listener, IMessageReceivedHandler,
													 Runnable {

	private CopyOnWriteArrayList<distributed.systems.das.server.State.GameState> states = new CopyOnWriteArrayList<distributed.systems.das.server.State.GameState> ();
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
	private TrailingStateSynchronization (distributed.systems.das.server.State.GameState startingState, int delayInterval,
										  int delays, int tickrate, Wishlist wishList) {

		if (delayInterval % tickrate != 0 && delayInterval > tickrate) {
			throw new IllegalArgumentException ("TSS delayInterval MUST be divisible by " +
												"tickrate and delayInterval MUST be larger than " +
												"tickrate!");
		}

		subscribeToNotifications (tickrate, wishList);
		long time = startingState.getTime ();

		for (int i = 0; i < delays; ++i) { // create states
			distributed.systems.das.server.State.GameState state = distributed.systems.das.server.State.GameState.clone (startingState);
			state.updateTime (time - (i * delayInterval)); // create trails
			this.states.add (state);
			this.delayIntervals.add (delayInterval);
		}

		try {
			start ();
		} catch (AlreadyRunningException e) {
			e.printStackTrace ();
		}
	}

	private synchronized void subscribeToNotifications (int tickrate, Wishlist wishList) {
		this.notify = new Notify (tickrate);
		try {
			this.notify.start ();
		} catch (AlreadyRunningException e) {
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
			distributed.systems.das.server.State.GameState state = states.get (i);
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
		distributed.systems.das.server.State.GameState beforeState = distributed.systems.das.server.State.GameState.clone (getState (0));
		distributed.systems.das.server.State.GameState afterState = getState (0);

		// Execute the command in the leading game state
		afterState.execute (event);

		Notify.Listener listener = new EventActionListener (i, beforeState, afterState,
															event);
		this.notify.subscribe (listener);
		return true;
	}

	// TODO: We don't need the whole state, we only need to be able to detect inconsistencies
	public synchronized distributed.systems.das.server.State.GameState getState (int index) {
		return this.states.get (index);
	}

	/**
	 * Returns whether the changes an event have made to two different states are the same
	 */
	public synchronized boolean compareStates (distributed.systems.das.server.State.GameState x, distributed.systems.das.server.State.GameState y) {
		return x == y;
	}

	@Override
	public void update (long time) {
		for (distributed.systems.das.server.State.GameState state : states) {
			state.updateTime (time);
		}
		if (!running) {
			this.running = true;
			this.thread = new Thread();
			this.thread.start ();
		}
	}

	@Override
	public Message onMessageReceived (Message event) {
//		addEvent (event);
		return null; // TODO fix type compatibility
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
		private distributed.systems.das.server.State.GameState before;
		private distributed.systems.das.server.State.GameState after;
		private long eventId;
		private Event event;

		private int counter;

		public EventActionListener (int index, distributed.systems.das.server.State.GameState before,
									distributed.systems.das.server.State.GameState after, Event event) {
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

			distributed.systems.das.server.State.GameState comparisonAfter = getState (index);
			distributed.systems.das.server.State.GameState comparisonBefore = distributed.systems.das.server.State.GameState.clone (comparisonAfter);
			comparisonAfter.execute (event);
			if (!compareStates (this.before, comparisonBefore) ||
				!compareStates (this.after, comparisonAfter)) {
				long time = this.after.getTime ();

				// TODO: More efficient way of replacing state? Perhaps only update select vars
				// 		 That would also get rid of the useless updateTime() call
				boolean success = this.after.replace (comparisonAfter);

				if (!success) {
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
		private distributed.systems.das.server.State.GameState startingState;
		private int delayInterval;
		private int delays;
		private int tickrate;
		private Wishlist wishList;

		public TSSBuilder (distributed.systems.das.server.State.GameState startingState) {
			this.startingState = startingState;
		}

		public int getDelayInterval () {
			return delayInterval;
		}

		public TSSBuilder setDelayInterval (int delayInterval) {
			this.delayInterval = delayInterval;
			return this;
		}

		public int getDelays () {
			return delays;
		}

		public TSSBuilder setDelays (int delays) {
			this.delays = delays;
			return this;
		}

		public int getTickrate () {
			return tickrate;
		}

		public TSSBuilder setTickrate (int tickrate) {
			this.tickrate = tickrate;
			return this;
		}

		public Wishlist getWishList () {
			return wishList;
		}

		public TSSBuilder setWishList (Wishlist wishList) {
			this.wishList = wishList;
			return this;
		}

		public TrailingStateSynchronization createTSS () {
			return new TrailingStateSynchronization (this.startingState, this.delayInterval,
													 this.delays, this.tickrate, this.wishList);
		}
	}

}

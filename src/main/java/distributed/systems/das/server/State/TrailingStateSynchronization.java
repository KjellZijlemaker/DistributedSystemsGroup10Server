package distributed.systems.das.server.State;

import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class TrailingStateSynchronization implements Notify.Listener {

	private CopyOnWriteArrayList<GameState> states = new CopyOnWriteArrayList<GameState> ();
	private CopyOnWriteArrayList<Integer> delayIntervals = new CopyOnWriteArrayList<> ();
	private EventList pendingEvents = new EventList ();
	private Notify notify;

	/**
	 * @param startingState
	 * @param delayInterval interval between the states. Must be divisible by {@code tickrate}
	 * @param delays        number of delays
	 * @param tickrate      rate at which time is updated
	 */
	public TrailingStateSynchronization (GameState startingState, int delayInterval, int delays,
										 int tickrate) {

		if (delayInterval % tickrate != 0) {
			throw new IllegalArgumentException ("TSS delayInterval MUST be divisble by tickrate!");
		}

		long time = startingState.getTime ();
		this.notify = new Notify (tickrate);
		try {
			this.notify.start ();
		} catch (Notify.AlreadyRunningException e) {
			Log.throwException (e, this.getClass ());
			// TODO: Handle this. Can probably just ignore, since it's already running.
		}
		this.notify.subscribe (this);

		for (int i = 0; i < delays; ++i) { // create states
			GameState state = GameState.clone (startingState);
			state.updateTime (time - (i * delayInterval)); // create trails
			this.states.add (state);
			this.delayIntervals.add (delayInterval);
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
	 */
	public synchronized void executeEvent () {
		int i = 1;
		Event event = pendingEvents.pop ();
		GameState beforeState = GameState.clone (getState (0));
		GameState afterState = getState (0);

		// Execute the command in the leading game state
		afterState.execute (event);

		Notify.Listener listener = new EventActionListener (i, beforeState, afterState,
															event.getId (), event);
		this.notify.subscribe (listener);
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
		for (int i = 0; i < states.size (); ++i) {
			GameState state = states.get (i);
			state.updateTime (time);
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
									GameState after, long eventId, Event event) {
			this.index = index;
			this.before = before;
			this.after = after;
			this.eventId = eventId;
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
					// TODO: Handle error
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

}

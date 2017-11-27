package distributed.systems.das.events;

import distributed.systems.das.GameState;
import distributed.systems.das.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class TrailingStateSynchronization implements Notify.Listener {

	private CopyOnWriteArrayList<GameState> states = new CopyOnWriteArrayList<GameState> ();
	private CopyOnWriteArrayList<Integer> delayIntervals = new CopyOnWriteArrayList<> ();
	private EventList pendingEvents = new EventList ();
	private Notify notify;

	/**
	 * @param startingState
	 * @param delayInterval interval between the states. Must be divible by <code>tickrate</code>
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
			GameState state = new GameState (startingState);
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
		GameState previousState = getState (0);

		// Execute the command in the leading game state
		previousState.execute (event);

		Notify.Listener listener = new EventActionListener (i, previousState, event.getId (),
															event);
		this.notify.subscribe (listener);
	}

	// TODO: We don't need the whole state, we only need to be able to detect inconsistencies
	public synchronized GameState getState (int index) {
		return this.states.get (index);
	}

	/**
	 * Returns whether the changes an event have made to two different states are the same
	 *
	 * @param x
	 * @param y
	 */
	public synchronized boolean compareChanges (GameState x, GameState y) {
		// TODO: Implement this.
		return true;
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
		private GameState previousState;
		private long eventId;
		private Event event;

		private int counter;

		public EventActionListener (int index, GameState previousState, long eventId, Event
				event) {
			this.index = index;
			this.previousState = previousState;
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

			GameState currentState = getState (index);
			currentState.execute (event);
			// TODO: Probably need to keep track of before and after states and compare diffs
			if (!compareChanges (previousState, currentState)) {
				long time = previousState.getTime ();

				// TODO: More efficient way of replacing state? Perhaps only update select vars
				// 		 That would also get rid of the useless updateTime() call
				boolean success = previousState.replace (currentState);

				if (!success) {
					// TODO: Handle error
				}

				// Set the time to the actual time. synchronize() will then process all the
				// events that now missing from the state because of overwriting the event list
				// with the event list from the older state.
				previousState.updateTime (time);
				previousState.synchronize ();
			}

			previousState = currentState;
			++this.index;
		}
	}

}

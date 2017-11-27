package distributed.systems.das.events;

import distributed.systems.das.GameState;
import distributed.systems.das.util.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrailingStateSynchronization implements Notify.Listener {

	private CopyOnWriteArrayList<GameState> states = new CopyOnWriteArrayList<GameState> ();
	private List<Integer> delayIntervals = new ArrayList<> ();
	private EventList pendingEvents = new EventList ();
	private Notify notify;

	/**
	 * @param startingState
	 * @param delayInterval
	 * @param delays        number of delays
	 * @param tickrate        rate at which time is updated
	 */
	public TrailingStateSynchronization (GameState startingState, int delayInterval, int delays,
										 int tickrate) {
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
	 * @param event
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

	public synchronized void executeEvent (Event event) {
		int i = 1;
		GameState previousState = getState (0);

		// Execute the command in the leading game state
		previousState.execute (event);

		ActionListener listener = new EventActionListener (i, previousState, event);

		// Check back after delayInterval.get(i) to check whether the results are correct
		for (i = i; i < states.size (); ++i) {
			Timer timer = new Timer (delayIntervals.get (i), listener);

		}
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
			long delay = delayIntervals.get (i);
			state.updateTime (time - delay);
		}
	}

	private class EventActionListener implements ActionListener {

		private int i;
		private GameState previousState;
		private Event event;

		public EventActionListener (int i, GameState previousState, Event event) {
			this.i = i;
			this.previousState = previousState;
			this.event = event;
		}

		@Override
		public void actionPerformed (ActionEvent e) {

			// TODO: might instead need to track event by its id, and not execute it here

			// "To detect inconsistencies, each trailing state looks at the changes in game
			// state that the execution of a command produced, and compares them with the
			// changes recorded in the directly preceding state"

			GameState currentState = getState (i);
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
			++this.i;
		}
	}

}

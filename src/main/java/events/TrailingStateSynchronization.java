package events;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TrailingStateSynchronization {

	private LinkedList<GameState> states = new LinkedList<GameState> ();
	private List<Integer> delayIntervals = new ArrayList<> ();
	private EventList pendingEvents = new EventList ();

	/**
	 * @param startingState
	 * @param delayInterval
	 * @param delays        number of delays
	 */
	public TrailingStateSynchronization (GameState startingState, int delayInterval, int delays) {
		long time = startingState.getTime ();
		for (int i = 0; i < delays; ++i) { // create states
			GameState state = new GameState (startingState);
			state.setTime (time - (i * delayInterval)); // create trails
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
		// TODO: execute events from here?
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
				// 		 That would also get rid of the useless setTime() call
				boolean success = previousState.replace (currentState);

				if (!success) {
					// TODO: Handle error
				}

				// Set the time to the actual time. synchronize() will then process all the
				// events that now missing from the state because of overwriting the event list
				// with the event list from the older state.
				previousState.setTime (time);
				previousState.synchronize ();
			}

			previousState = currentState;
			++this.i;
		}
	}

}

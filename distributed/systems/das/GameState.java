package distributed.systems.das;

import distributed.systems.das.events.Event;
import distributed.systems.das.events.EventQueue;

/**
 * Class containing the global gamestate. This
 * state contains small things, which all threads 
 * need to know. 
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class GameState {
	// Is-the-program-actually-running-flag
	private static volatile boolean running = true;
	// Relation between game time and real time
	public static final double GAME_SPEED = .01;
	// The number of players in the game
	private static int playerCount = 0;

	private long time = 0;
	private EventQueue eventQueue;

	public GameState (long time, EventQueue eventQueue) {
		this.time = time;
		this.eventQueue = eventQueue;
	}

	/**
	 * Runs an event. TODO: Write these classes and have them use subclasses of Event
	 * @param event the event to execute
	 * @return true if successful
	 */
	public boolean execute (Event event) {
		// move, attack, etc.
		return true;
	}

	/**
	 * Replaces the current game state with a new one.
	 *
	 * @param newState the new state of the game
	 * @return true if successful
	 */
	public boolean replace (GameState newState) {

//		TODO: copy over the actual game state, including all the logic, etc.
//		This is assuming that this class is the one that will preside over the all the game
//		variables.
		this.eventQueue.clear ();
		return this.eventQueue.addAll (newState.getEventQueue ());

	}

	public synchronized long getTime () {
		return this.time;
	}

	public synchronized EventQueue getEventQueue () {
		return this.eventQueue;
	}

	/**
	 * Stop the program from running. Inform all threads
	 * to close down.
	 */
	public static void haltProgram() {
		running = false;
	}

	/**
	 * Get the current running state 
	 * @return true if the program is supposed to 
	 * keep running.
	 */
	public static boolean getRunningState() {
		return running;
	}

	/**
	 * Get the number of players currently in the game.
	 * @return int: the number of players currently in the game.
	 */
	public static int getPlayerCount() {
		return playerCount;
	}
}

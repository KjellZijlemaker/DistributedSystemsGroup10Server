package distributed.systems.das.server.events;

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

	private long time;
	private long lastUpdate;
	private EventList eventList;

	public GameState (long time, EventList eventList) {
		this.time = time;
		this.lastUpdate = time;
		this.eventList = eventList;
	}

	public GameState (GameState newState) {
		replace (newState);
	}

	/**
	 * Runs an event. TODO: Write these classes and have them use subclasses of Event
	 * @param event the event to execute
	 * @return true if successful
	 */
	public synchronized boolean execute (Event event) {
		// move, attack, etc.
		return true;
	}

	/**
	 * Replaces the current game state with a new one.
	 *
	 * @param newState the new state of the game
	 * @return true if successful
	 */
	public synchronized boolean replace (GameState newState) {

//		TODO: copy over the actual game state, including all the logic, etc.
//		This is assuming that this class is the one that will preside over the all the game
//		variables.
		this.eventList.clear ();
		return this.eventList.addAll (newState.getEventList ());
	}

	/**
	 * Executes all the events that have happened since the time value for this object
	 * @return
	 */
	public synchronized boolean synchronize () {
		// TODO: Handle all the events that have happened since current time
		return true;
	}

	public synchronized long getTime () {
		return this.time;
	}

	public synchronized void setTime (long time) {
		this.time = time;
	}

	public synchronized EventList getEventList () {
		return this.eventList;
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

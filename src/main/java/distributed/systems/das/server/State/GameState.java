package distributed.systems.das.server.State;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Units.Player;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Class containing the global gamestate. This
 * state contains small things, which all threads
 * need to know.
 *
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class GameState implements IMessageReceivedHandler {
    static final Logger Log = LoggerFactory.getLogger(GameState.class);
    // Is-the-program-actually-running-flag
    private static volatile boolean running = true;
    // Relation between game time and real time
    public static final double GAME_SPEED = .01;
    // The number of players in the game
    private static int playerCount = 0;

    private long time;
    private long lastUpdate;
    private EventList eventList;
    private BattleField battleField;

    public GameState(long time, EventList eventList) {
        this.time = time;
        this.lastUpdate = time;
        this.eventList = eventList;
        this.battleField = BattleField.getBattleField();
    }

    private GameState(long time, EventList eventList, long lastUpdate, BattleField battleField) {
        this.time = time;
        this.lastUpdate = lastUpdate;
        this.eventList = eventList;
        this.battleField = battleField;
    }

    public static GameState clone(GameState toCopy) {
        return new GameState(
                toCopy.getTime(),
                new EventList(toCopy.getEventList()),
                toCopy.getLastUpdate(),
                BattleField.clone(toCopy.getBattleField()));
    }

    public Message connectUser(Message message) {
        Unit remotePlayer = initPlayer(message.actorID);
        boolean success = populatePlayer(remotePlayer);
        if (!success) {
            return null;
        }

        Log.info("User connected with ID " + remotePlayer.getUnitID());
//        userCallbacks.put(remotePlayer.getUnitID(), callback);
//        players.add(remotePlayer);

        Message response = new Message(message);
        response.body.put("battlefield", battleField);
        response.body.put("unit", remotePlayer);
        return response;
    }

    private Player initPlayer(String unitID) {
        int hp = new Random().nextInt(11) + 10;
        int ap = new Random().nextInt(10) + 1;
        return new Player(hp, ap, unitID);
    }

    private boolean populatePlayer(Unit remotePlayer) {
        int x, y, attempt = 0;

        do {
            x = (int) (Math.random() * BattleField.MAP_WIDTH);
            y = (int) (Math.random() * BattleField.MAP_HEIGHT);
            attempt++;
        } while (!battleField.spawnUnit(remotePlayer, x, y) && attempt < 10);

        if (!battleField.getUnit(x, y).getUnitID()
                .equals(remotePlayer.getUnitID())) {
            return false;
        }

        remotePlayer.setPosition(x, y);
        return true;
    }

    public boolean populateDragon(Unit localDragon){

			/* Try picking a random spot */
            int x, y, attempt = 0;
            do {
                x = (int) (Math.random() * BattleField.MAP_WIDTH);
                y = (int) (Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (!battleField.spawnUnit(localDragon, x, y) && attempt < 10);

        if (!battleField.getUnit(x, y).getUnitID()
                .equals(localDragon.getUnitID())) {
            return false;
        }
        localDragon.setPosition(x, y);

        return true;
    }

    public void disconnectUser(Unit player) {
//        players.remove(player);
//        userCallbacks.remove(player.getUnitID());
        battleField.removeUnit(player.getUnitID());
        Log.info("User: " + player.getUnitID() + " disconnected");
    }

    public void disconnectUser(String unitID) {
        Unit unit = battleField.getUnits().stream()
                .filter(x -> x.getUnitID().equals(unitID))
                .findFirst()
                .orElseGet(null);
        disconnectUser(unit);
    }

    synchronized Message execute(Message message) {
//		eventList.add (message);
        switch (message.type) {
            case Message.LOGIN:
                return connectUser(message);
            case Message.ATTACK:
                battleField.attack(message);
                break;
            case Message.HEAL:
                battleField.heal(message);
                break;
            case Message.MOVE:
                battleField.move(message);
                break;
        }
        return new Message(message); // TODO return proper message
    }

    /**
     * Replaces the current game state with a new one.
     *
     * @param newState the new state of the game
     * @return true if successful
     */
    public synchronized boolean replace(GameState newState) {

//		TODO: copy over the actual game state, including all the logic, etc.
//		This is assuming that this class is the one that will preside over the all the game
//		variables.
        this.eventList.clear();
        return this.eventList.addAll(newState.getEventList());
    }

    /**
     * Executes all the events that have happened since the time value for this object
     *
     * @return true if successful
     */
    public synchronized boolean synchronize() {
        // TODO: Handle all the events that have happened since current time

        for (Message event : this.eventList.getEventsByTime (this.lastUpdate, this.time)) {
//            execute(event);
        }

        this.lastUpdate = this.time;
        return true;
    }

    public synchronized long getTime() {
        return this.time;
    }

    public synchronized void setTime(long time) {
        this.time = time;
    }

    /**
     * Increments the time by specified amount
     */
    public synchronized void updateTime(long time) {
        this.time += time;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public synchronized EventList getEventList() {
        return this.eventList;
    }

    public synchronized BattleField getBattleField() {
        return this.battleField;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }
        GameState state = (GameState) obj;


        return (this.time == state.getTime()) &&
                (this.eventList == state.getEventList()) &&
                (this.battleField == state.getBattleField());
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
     *
     * @return true if the program is supposed to
     * keep running.
     */
    public static boolean getRunningState() {
        return running;
    }

    /**
     * Get the number of players currently in the game.
     *
     * @return int: the number of players currently in the game.
     */
    public static int getPlayerCount() {
        return playerCount;
    }

    /**
     * Sets the number of players currently in the game
     */
    public static void setPlayerCount(int players) {
        playerCount = players;
    }

    @Override
    public Message onMessageReceived(Message event) {
        return execute(event);
    }
}

package distributed.systems.das.server.State;

import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;

public class BattleField implements Serializable {
    static final Logger Log = LoggerFactory.getLogger(BattleField.class);
    /* The array of units */
    private Unit[][] map;

    private static final long serialVersionUID = 10L;

    /* The static singleton */
    private static BattleField battlefield;

    public final static int MAP_WIDTH = 25;
    public final static int MAP_HEIGHT = 25;
    private ArrayList<Unit> units;

    /**
     * Initialize the battlefield to the specified size
     *
     * @param width  of the battlefield
     * @param height of the battlefield
     */
    private BattleField(int width, int height) {
        synchronized (this) {
            this.map = new Unit[width][height];
            this.units = new ArrayList<Unit>();
        }
    }
    
    public boolean isLegalCoordinates(int x, int y) {
    	return x>-1 && x<MAP_WIDTH && y>-1 && y<MAP_HEIGHT;
    }

    private BattleField(int width, int height, Unit[][] map, ArrayList<Unit> units) {
        synchronized (this) {
            this.map = new Unit[width][height];
            for (int x = 0; x < width; ++x) {
                System.arraycopy(map[x], 0, this.map[x], 0, height);
            }
            this.units = new ArrayList<Unit>(units);
        }
    }

    public static BattleField clone(BattleField battleField) {
        Unit[][] map = battleField.getMap();
        return new BattleField(map.length,
                map[0].length,
                map,
                battleField.getUnits());
    }

    /**
     * Singleton method which returns the sole
     * instance of the battlefield.
     *
     * @return the battlefield.
     */
    public static BattleField getBattleField() {
        if (battlefield == null)
            battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
        return battlefield;
    }

    /**
     * Puts a new unit at the specified position. First, it
     * checks whether the position is empty, if not, it
     * does nothing.
     * In addition, the unit is also put in the list of known units.
     *
     * @param unit is the actual unit being spawned
     *             on the specified position.
     * @param x    is the x position.
     * @param y    is the y position.
     * @return true when the unit has been put on the
     * specified position.
     */
    public boolean spawnUnit(Unit unit, int x, int y) {
        synchronized (this) {
            if (map[x][y] != null)
                return false;

            map[x][y] = unit;
            unit.setPosition(x, y);
            Log.debug("Spawn " + unit.getUnitID() + " on location " + x + " " + y);
        }
        units.add(unit);

        return true;
    }

    /**
     * Put a unit at the specified position. First, it
     * checks whether the position is empty, if not, it
     * does nothing.
     *
     * @param unit is the actual unit being put
     *             on the specified position.
     * @param x    is the x position.
     * @param y    is the y position.
     * @return true when the unit has been put on the
     * specified position.
     */
    public synchronized boolean putUnit(Unit unit, int x, int y) {
        if (map[x][y] != null)
            return false;

        map[x][y] = unit;
        unit.setPosition(x, y);

        Log.debug("putUnit "+unit.getUnitID()+" on "+x+" "+y);
        return true;
    }

    /**
     * Get a unit from a position.
     *
     * @param x position.
     * @param y position.
     * @return the unit at the specified position, or return
     * null if there is no unit at that specific position.
     */
    public Unit getUnit(int x, int y) {
        assert x >= 0 && x < map.length;
        assert y >= 0 && x < map[0].length;

        return map[x][y];
    }

    public Unit getUnit(String unitID) {
        return units.stream()
                .filter(x -> x.getUnitID()
                        .equals(unitID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Move the specified unit a certain number of steps.
     *
     * @param unit is the unit being moved.
     * @param newX is the delta in the x position.
     * @param newY is the delta in the y position.
     * @return true on success.
     */
    private synchronized boolean moveUnit(Unit unit, int newX, int newY) {
        int originalX = unit.getX();
        int originalY = unit.getY();

        if (unit.getHitPoints() <= 0)
            return false;

        if (newX >= 0 && newX < BattleField.MAP_WIDTH)
            if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
                if (map[newX][newY] == null) {
                    if (putUnit(unit, newX, newY)) {
                        map[originalX][originalY] = null;
                        return true;
                    }
                }

        return false;
    }

    /**
     * Remove a unit from a specific position and makes the unit disconnect from the server.
     *
     * @param x position.
     * @param y position.
     */
    public synchronized void removeUnit(int x, int y) {
        Unit unitToRemove = this.getUnit(x, y);
        if (unitToRemove == null)
            return; // There was no unit here to remove
        map[x][y] = null;
        units.remove(unitToRemove);
        Log.debug("removeUnit "+unitToRemove.getUnitID()+ " from "+x+" "+y);
    }

    public void removeUnit(String unitID) {
        Unit u = getUnit(unitID);
        removeUnit(u.getX(), u.getY());
    }

    public void attack(Message message) {
        int x = (Integer) message.body.get("x");
        int y = (Integer) message.body.get("y");
        Unit unit = this.getUnit(x, y);
        if (unit != null) {
            unit.adjustHitPoints(-(Integer) message.body.get("damage"));
            if (unit.isDead()) removeUnit(x, y);
        }
    }

    public void heal(Message message) {
        int x = (Integer) message.body.get("x");
        int y = (Integer) message.body.get("y");
        Unit unit = this.getUnit(x, y);
        if (unit != null) {
            unit.adjustHitPoints((Integer) message.body.get("healed"));
        }
    }

    public void move(Message message) {
        int x = (Integer) message.body.get("x");
        int y = (Integer) message.body.get("y");
        Unit unit = getUnit(message.actorID);
        moveUnit(unit, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BattleField battleField = (BattleField) obj;
        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                if (map[x][y] == battleField.getUnit(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Unit[][] getMap() {
        return map;
    }

    public synchronized void setMap(Unit[][] map) {
        this.map = map;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public synchronized void setUnits(ArrayList<Unit> units) {
        this.units = units;
    }

}

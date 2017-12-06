package distributed.systems.das.server.Units;

public class Player extends Unit{

    /**
     * Create a new unit and specify the
     * number of hitpoints. Units hitpoints
     * are initialized to the maxHitPoints.
     *
     * @param maxHealth    is the maximum health of
     *                     this specific unit.
     * @param attackPoints
     */
    public Player(int maxHealth, int attackPoints, String playerID) {
        super(maxHealth, attackPoints, playerID);
    }
    
    public void logStatus() {
    	System.out.println("---Status for Player " + this.getUnitID() + "---");
    	System.out.println("coordinats: " + this.getX() + "," + this.getY());
    	System.out.println("health points: " + this.getHitPoints() + "/" + this.getMaxHitPoints());
    	System.out.println("---End Status---");
    }

}
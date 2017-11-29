package distributed.systems.das.server;

import java.io.Serializable;

/**
 * Base class for all players whom can
 * participate in the DAS game. All properties
 * of the units (hitpoints, attackpoints) are
 * initialized in this class.
 *
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class Unit implements Serializable {
	private static final long serialVersionUID = -4550572524008491160L;

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;

	public enum Direction {
		up, right, down, left
	}

	public enum UnitType {
		player, dragon, undefined,
	}

	/**
	 * Create a new unit and specify the
	 * number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints.
	 *
	 * @param maxHealth is the maximum health of
	 *                  this specific unit.
	 */
	public Unit (int maxHealth, int attackPoints) {
		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		unitID = BattleField.getBattleField ().getNewUnitID ();
	}

	/**
	 * Adjust the hitpoints to a certain level.
	 * Useful for healing or dying purposes.
	 *
	 * @param modifier is to be added to the
	 *                 hitpoint count.
	 */
	public synchronized void adjustHitPoints (int modifier) {
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

		if (hitPoints <= 0)
			removeUnit (x, y);
	}

	public void dealDamage (int x, int y, int damage) {
		// TODO: might need this
	}

	public void healDamage (int x, int y, int healed) {
		// TODO: might need this
	}

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints () {
		return maxHitPoints;
	}

	/**
	 * @return the unique unit identifier.
	 */
	public int getUnitID () {
		return unitID;
	}

	/**
	 * Set the position of the unit.
	 *
	 * @param x is the new x coordinate
	 * @param y is the new y coordinate
	 */
	public void setPosition (int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x position
	 */
	public int getX () {
		return x;
	}

	/**
	 * @return the y position
	 */
	public int getY () {
		return y;
	}

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints () {
		return hitPoints;
	}

	/**
	 * @return the attack points
	 */
	public int getAttackPoints () {
		return attackPoints;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 *
	 * @param x x-coordinate of the spawn location
	 * @param y y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
	protected boolean spawn (int x, int y) {
		// TODO: Might need this
		// Wait for the unit to be placed
		getUnit (x, y);

		return true;
	}

	/**
	 * Returns whether the indicated square contains a player, a dragon or nothing.
	 *
	 * @param x: x coordinate
	 * @param y: y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or nothing.
	 */
	protected void getType (int x, int y) {
		// TODO: Might need this

	}

	protected Unit getUnit (int x, int y) {
		// TODO: might need this
		return this;
	}

	protected void removeUnit (int x, int y) {
		// TODO: might need this
	}

	protected void moveUnit (int x, int y) {
		// TODO: might need this
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}

		if (this.getClass () != obj.getClass ()) {
			return false;
		}
		Unit unit = (Unit) obj;
		return (this.x == unit.getX ()) && (this.y == unit.getY ())
				&& (this.unitID == unit.getUnitID ());
	}
}
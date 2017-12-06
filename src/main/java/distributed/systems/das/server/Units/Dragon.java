package distributed.systems.das.server.Units;

import distributed.systems.das.server.State.GameState;

import java.util.ArrayList;

public class Dragon extends Unit implements Runnable{
    /* Reaction speed of the dragon
	 * This is the time needed for the dragon to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
    protected int timeBetweenTurns;
    public static final int MIN_TIME_BETWEEN_TURNS = 2;
    public static final int MAX_TIME_BETWEEN_TURNS = 7;
    // The minimum and maximum amount of hitpoints that a particular dragon starts with
    public static final int MIN_HITPOINTS = 50;
    public static final int MAX_HITPOINTS = 100;
    // The minimum and maximum amount of hitpoints that a particular dragon has
    public static final int MIN_ATTACKPOINTS = 5;
    public static final int MAX_ATTACKPOINTS = 20;
    private boolean running;
    private String dragonID;


    public Dragon(String UUID) {
                /* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
        super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS), UUID);

		/* Create a random delay */
        timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

    }


    @Override
    public void run() {
		ArrayList<Player> adjacentPlayers = new ArrayList<> ();

        this.running = true;


        while(GameState.getRunningState() && this.running) {
            try {
				/* Sleep while the dragon is considering its next move */
                Thread.sleep((int) (timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the dragon runs out of hitpoints */
                if (getHitPoints() <= 0)
                    break;

                // Decide what players are near
				int x = getX ();
				int y = getY ();
				for (int i = (x - 2); i <= (x + 2); ++i) {
					for (int j = (y - 2); j <= (y + 2); ++y) {
						if (getType (i, j) == UnitType.player) {
							Player player = new Player (10, 10, "null");
							player.setPosition (i, j);
							adjacentPlayers.add (player);
						}
					}
				}
				// Pick a random player to attack
				if (adjacentPlayers.size() == 0)
                    continue; // There are no players to attack

				Player playerToAttack = adjacentPlayers.get ((int) (Math.random () *
						adjacentPlayers.size ()));

				this.dealDamage (playerToAttack.getX (), playerToAttack.getY
						(), this.getAttackPoints ());
			} catch (InterruptedException e) {
				e.printStackTrace();
            }
        }
    }

}

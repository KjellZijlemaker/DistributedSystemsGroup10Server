package distributed.systems.das.server.Units;

import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.GameState;

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
	private BattleField battleField;

	private DragonAttack listener;

	public Dragon (String UUID) {
				/* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
		super ((int) (Math.random () * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int) (Math.random () * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS), UUID);

		new DragonAttack (this);


		/* Create a random delay */
		timeBetweenTurns = (int) (Math.random () * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
	}

	public void subscribe (DragonAttack listener) {
		this.listener = listener;
	}

	@Override
	public void run () {

		while (GameState.getRunningState ()) {
			try {
				/* Sleep while the dragon is considering its next move */
//				Thread.sleep ((int) (timeBetweenTurns * 1000 * GameState.GAME_SPEED));
				Thread.sleep(3000);

				/* Stop if the dragon runs out of hitpoints */
				if (getHitPoints () <= 0)
					break;

				System.out.println("Dragon (" + this.getX() + "," + this.getY() + ") [" + this.getHitPoints() + "] "+ this.getUnitID() + " try to attack");

				this.listener.attack (getX (), getY (), getAttackPoints ());
			} catch (Exception e) {
				e.printStackTrace ();
			}
		}
	}

	// Decide what players are near
//				int x = getX ();
//				int y = getY ();
//				for (int i = (x - 2); i <= (x + 2); ++i) {
//					int nullCount = 0;
//					for (int j = (y - 2); j <= (y + 2); ++j) {
////						if (Math.abs(x - i) + Math.abs(j - y) > 2) {
////							continue;
////						}
////						if (!battleField.isLegalCoordinates(i, j)) {
////							continue;
////						}
//						System.out.println ("OVER");
//
//
//						if (battleField.getUnit (i, j) != null) {
//							System.out.println ((("UNIT TYPE: " +
//									battleField.getUnit (i, j)
//											.getType ())));
//							if (battleField.getUnit (i, j).getType () == Unit
//									.PLAYER) {
//								Player player = new Player (10, 10, "null");
//								player.setPosition (i, j);
//								adjacentPlayers.add (player);
//							}
//						} else nullCount++;
//
//						System.out.println ("nullCOunt is " + nullCount);
//					}
//				}
//				// Pick a random player to attack
//				if (adjacentPlayers.size() == 0)
//					continue; // There are no players to attack
//
//				Player playerToAttack = adjacentPlayers.get ((int) (Math.random () *
//						adjacentPlayers.size ()));
//
//				Message m = new Message(1, System.currentTimeMillis(),
//						playerToAttack.getUnitID (), Message.ATTACK);
//				m.body.put ("x", x);
//				m.body.put ("y", y);
//				m.body.put("damage", this.getAttackPoints ());
//
//				System.out.println ("Dragon attack");
//				try {
//					Registry reg = LocateRegistry.getRegistry("localhost", 5001);
//					for (int i = 1; i < reg.list ().length; ++i) {
//						IMessageReceivedHandler player =
//								(IMessageReceivedHandler) reg.lookup(reg.list
//										()[i]);
//						player.onMessageReceived (m);
//
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public int getType () {
		return DRAGON;
	}

}



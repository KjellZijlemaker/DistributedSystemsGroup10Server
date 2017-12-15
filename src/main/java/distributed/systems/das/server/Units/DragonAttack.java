package distributed.systems.das.server.Units;

import distributed.systems.das.server.ServerRunner;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.events.Message;

import java.io.Serializable;
import java.util.ArrayList;

public class DragonAttack implements DragonListener, Serializable {

	private Dragon dragon;

	public DragonAttack (Dragon dragon) {
		this.dragon = dragon;
		dragon.subscribe (this);
	}

	@Override
	public boolean attack (int x, int y, int damage) {
		ArrayList<Player> adjacentPlayers = new ArrayList<> ();
		TrailingStateSynchronization tss = ServerRunner.getTSS ();
		BattleField battleField = tss.getLeadingBattleField ();

		for (int i = (x - 2); i <= (x + 2); ++i) {
			for (int j = (y - 2); j <= (y + 2); ++j) {
				if (Math.abs(x - i) + Math.abs(j - y) > 2) {
					continue;
				}

				// ensure that (i,j) is not out of the battlefiled
				if (!battleField.isLegalCoordinates (i, j)) {
					continue;
				}

				if (battleField.getUnit (i, j) != null) {
//					System.out.println ((("UNIT TYPE: " +
//							battleField.getUnit (i, j)
//									.getType ())));
					if (battleField.getUnit (i, j).getType () == Unit
							.PLAYER) {
						System.out.println ("Dragon find player within attack range at "+i+","+j);

						Player player = (Player)battleField.getUnit(i,j);
						adjacentPlayers.add (player);
					}
				}
			}
		}
		// Pick a random player to attack
		if (adjacentPlayers.size () == 0) {
			System.out.println("Dragon " + dragon.getUnitID() + " finds no players within attack range");
			return false; // There are no players to attack
		}

		Player playerToAttack = adjacentPlayers.get (0);

		System.out.println("Dragon" + dragon.getUnitID() + " try to attack player " + playerToAttack.getUnitID());

		Message m = new Message (1, System.currentTimeMillis (),
				playerToAttack.getUnitID (), Message.ATTACK);
		m.body.put ("x", playerToAttack.getX ());
		m.body.put ("y", playerToAttack.getY ());
		m.body.put ("damage", damage);
		m.body.put ("_from", "dragon");

		tss.executeEvent (m);
		return true;
	}

}

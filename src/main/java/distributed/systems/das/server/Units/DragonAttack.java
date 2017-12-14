package distributed.systems.das.server.Units;

import distributed.systems.das.server.ServerRunner;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.events.Message;

import java.io.Serializable;
import java.util.ArrayList;

public class DragonAttack implements DragonListener, Serializable {

	public DragonAttack (Dragon dragon) {
		dragon.subscribe (this);
	}

	@Override
	public boolean attack (int x, int y, int damage) {
		ArrayList<Player> adjacentPlayers = new ArrayList<> ();
		TrailingStateSynchronization tss = ServerRunner.getTSS ();
		BattleField battleField = tss.getLeadingBattleField ();


		for (int i = (x - 2); i <= (x + 2); ++i) {
			for (int j = (y - 2); j <= (y + 2); ++j) {
//				if (Math.abs(x - i) + Math.abs(j - y) > 2) {
//					continue;
//				}
				if (!battleField.isLegalCoordinates (i, j)) {
					continue;
				}

				if (battleField.getUnit (i, j) != null) {
//					System.out.println ((("UNIT TYPE: " +
//							battleField.getUnit (i, j)
//									.getType ())));
					if (battleField.getUnit (i, j).getType () == Unit
							.PLAYER) {
						System.out.println ("Dragon attacks player at "+i+","+j);
						Player player = new Player (10, 10, battleField.getUnit(i, j).getUnitID());
						player.setPosition (i, j);
						adjacentPlayers.add (player);
						break;
					}
				}
			}
		}
		// Pick a random player to attack
		if (adjacentPlayers.size () == 0)
			return false; // There are no players to attack

		Player playerToAttack = adjacentPlayers.get (0);

		Message m = new Message (1, System.currentTimeMillis (),
				playerToAttack.getUnitID (), Message.ATTACK);
		m.body.put ("x", playerToAttack.getX ());
		m.body.put ("y", playerToAttack.getY ());
		m.body.put ("damage", damage);

		tss.executeEvent (m);
		return true;
	}

}

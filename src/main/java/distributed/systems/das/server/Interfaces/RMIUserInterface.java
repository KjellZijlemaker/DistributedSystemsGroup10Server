package distributed.systems.das.server.Interfaces;


import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.Units.Player;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Event;
import org.javatuples.Triplet;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface will force the implementation of the given methods below
 */
public interface RMIUserInterface extends Remote {
    Triplet<Boolean, BattleField, Unit> connectUser(Unit p, RMISendToUserInterface c) throws RemoteException;

    void disconnectUser(Unit remotePlayer) throws RemoteException;

    String registerWish(Unit remoteObject, Event event) throws RemoteException;
}

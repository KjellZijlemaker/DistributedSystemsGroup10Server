package distributed.systems.das.server.Interfaces;


import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.events.Event;
import org.javatuples.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface will force the implementation of the given methods below
 */
public interface RMIUserInterface extends Remote {
    String connectUser(Pair userObjectPair) throws RemoteException;
    void disconnectUser(Pair userObjectPair) throws RemoteException;
    String registerWish(Pair userObject, Event event) throws RemoteException;
}

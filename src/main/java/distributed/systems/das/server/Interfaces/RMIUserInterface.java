package distributed.systems.das.server.Interfaces;


import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.events.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface will force the implementation of the given methods below
 */
public interface RMIUserInterface extends Remote {
    public String connectUser(User user) throws RemoteException;
    public String registerWish(User user, Event event) throws RemoteException;
}

package distributed.systems.das.server.Interfaces;


import distributed.systems.das.server.Beans.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface will force the implementation of the given methods below
 */
public interface RMIUserInterface extends Remote {
    public String connectUser(User user) throws RemoteException;
}

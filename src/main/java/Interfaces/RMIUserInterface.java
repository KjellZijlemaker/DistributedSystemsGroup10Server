package Interfaces;

import Beans.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface will force the implementation of the given methods below
 */
public interface RMIUserInterface extends Remote {
    public String connectUser(User user) throws RemoteException;
}

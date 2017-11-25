package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.Beans.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameStateInfo extends Remote {
    void register(RMIGameStateUpdate o, String userID, String password) throws RemoteException;
    void unregister(RMIGameStateUpdate o) throws RemoteException;
    String moveParameter(String move, RMIGameStateUpdate o, String userID) throws RemoteException;
}

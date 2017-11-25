package distributed.systems.das.server.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameStateInfo extends Remote {
    void register(RMIGameStateUpdate o) throws RemoteException;
    void unregister(RMIGameStateUpdate o) throws RemoteException;
    String moveParameter(String move) throws RemoteException;
 }

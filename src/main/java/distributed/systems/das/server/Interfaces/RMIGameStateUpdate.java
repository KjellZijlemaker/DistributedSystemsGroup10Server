package distributed.systems.das.server.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameStateUpdate extends Remote {
    void update(boolean getRunningState , int playerCount) throws RemoteException;
}

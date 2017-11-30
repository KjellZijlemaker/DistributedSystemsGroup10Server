package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.State.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISendToUserInterface extends Remote {
    void update(String test) throws RemoteException;
}

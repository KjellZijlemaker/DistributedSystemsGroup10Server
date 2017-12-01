package distributed.systems.das.server.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISendToUserInterface extends Remote {
    void update(String test) throws RemoteException;
}

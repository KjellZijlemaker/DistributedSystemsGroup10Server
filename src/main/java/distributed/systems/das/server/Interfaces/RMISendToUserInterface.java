package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.events.EventList;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISendToUserInterface extends Remote {
    void update(EventList events) throws RemoteException;
}

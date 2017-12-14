package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISendToUserInterface extends Remote {
    void update(Message message) throws RemoteException;
    Message getUpdate() throws RemoteException;
}

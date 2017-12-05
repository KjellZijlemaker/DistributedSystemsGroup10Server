package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Callback extends UnicastRemoteObject implements RMISendToUserInterface {
    private Message message;

    public Callback() throws RemoteException {
        super();
    }


    @Override
    public void update(Message message) throws RemoteException {
        this.message = message;
    }

    @Override
    public Message getUpdate() throws RemoteException {
        return this.message;
    }

}

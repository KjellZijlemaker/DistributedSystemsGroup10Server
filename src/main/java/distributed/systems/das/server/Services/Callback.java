package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Callback extends UnicastRemoteObject implements RMISendToUserInterface {
    private Event event;

    public Callback() throws RemoteException {
        super();
    }


    @Override
    public void update(Event events) throws RemoteException {
        this.event = events;
    }

    public Event getEventlist(){
        return this.event;
    }

}

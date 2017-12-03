package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.events.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageReceivedHandler extends Remote {

    Message onMessageReceived(Message event) throws RemoteException;
}

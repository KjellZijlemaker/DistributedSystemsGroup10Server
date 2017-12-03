package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class MessageBroker extends UnicastRemoteObject implements IMessageReceivedHandler {
    static final Logger Log = LoggerFactory.getLogger(MessageBroker.class);
    private static final long serialVersionUID = 1L;
    private Map<Integer, IMessageReceivedHandler> listeners = new HashMap<>();

    public MessageBroker() throws RemoteException {
        super();
    }

    @Override
    public Message onMessageReceived(Message message) throws RemoteException {
        return listeners.get(message.type).onMessageReceived(message);
    }

    public void registerListener(int type, IMessageReceivedHandler handler) {
        listeners.put(type, handler);
    }
}

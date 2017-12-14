package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class ClientPlayHandler implements IMessageReceivedHandler {
    static final Logger Log = LoggerFactory.getLogger(ClientPlayHandler.class);
    private final ServerHandler serverHandler;

    public ClientPlayHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public Message onMessageReceived(Message message) throws RemoteException {
        serverHandler.serverRegistry.forEach((key, value) -> {
            try {
                value.onMessageReceived(message);
                Log.debug("send player event to "+key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        return message;
    }
}

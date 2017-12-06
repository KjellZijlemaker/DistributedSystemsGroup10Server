package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler implements IMessageReceivedHandler {
    static final Logger Log = LoggerFactory.getLogger(ServerHandler.class);

    public static final String REGISTRY_PORT = "registry_port";
    Map<String, IMessageReceivedHandler> serverRegistry = new ConcurrentHashMap<>();

    public void doHandshake(int otherServerPort, int myPort, String serverID) throws Exception {
        Registry remoteRegistry = LocateRegistry.getRegistry("localhost", otherServerPort);
        String otherServerID = Arrays.stream(remoteRegistry.list())
                .filter(s -> s.contains("server"))
                .findFirst()
                .orElse("");

        System.out.println("123 "+Arrays.toString(remoteRegistry.list()));
        IMessageReceivedHandler otherServer = (IMessageReceivedHandler) remoteRegistry.lookup(otherServerID);

        Message handshake = new Message(0, System.currentTimeMillis(), serverID, Message.HANDSHAKE);
        handshake.body.put(REGISTRY_PORT, myPort);
        otherServer.onMessageReceived(handshake); // TODO get battlefield
        serverRegistry.put(otherServerID, otherServer);
        Log.info("doHandshake otherServerPort: "+otherServerPort+ " otherServerID: "+otherServerID);
    }

    @Override
    public Message onMessageReceived(Message message) throws RemoteException {
        String otherServerID = message.actorID;
        int otherServerPort = (int) message.body.get("registry_port");

        Registry remoteRegistry = LocateRegistry.getRegistry("localhost", otherServerPort);
        try {
            IMessageReceivedHandler otherServer = (IMessageReceivedHandler) remoteRegistry.lookup(otherServerID);
            serverRegistry.put(otherServerID, otherServer);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        Log.info("receiveHandshake otherServerID: "+otherServerID);
        return new Message(message);
    }
}

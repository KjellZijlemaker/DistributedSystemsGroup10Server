package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Services.ClientPlayHandler;
import distributed.systems.das.server.Services.HeartbeatService;
import distributed.systems.das.server.Services.MessageBroker;
import distributed.systems.das.server.Services.ServerHandler;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.Units.Dragon;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.UUID;

public class ServerRunner {

    private final static String serverID = "server-" + UUID.randomUUID ().toString ();
    private static final int DRAGON_COUNT = 20;
    private final MessageBroker broker = new MessageBroker();
    private ServerHandler serverHandler;

    ServerRunner(String args[]) throws Exception {
        int myPort = Integer.parseInt(args[0]);
        startServer(myPort);

        if (args.length == 1) {
            return;
        }

        int otherServerPort = Integer.parseInt(args[1]);
        connectToOtherServer(otherServerPort, myPort);
    }

    private static TrailingStateSynchronization tss;

    public static void main(String args[]) throws Exception {
        ServerRunner runner = new ServerRunner(args);
    }

    private void connectToOtherServer(int otherServerPort, int myPort) throws Exception {
        serverHandler.doHandshake(otherServerPort, myPort, serverID);
    }

    private void startServer(int myPort) throws RemoteException, AlreadyBoundException, MalformedURLException {
        String myRMIAddress = String.format("//localhost:%d/%s", myPort, serverID);

        EventList eventList = new EventList();
        GameState localGameState = new GameState(1,eventList);
        serverHandler = new ServerHandler(localGameState);
        IMessageReceivedHandler clientPlayHandler = new ClientPlayHandler(serverHandler);
        HeartbeatService heartbeatService = new HeartbeatService(localGameState);
        new Thread(heartbeatService).start();

        TrailingStateSynchronization tss =
                new TrailingStateSynchronization.TSSBuilder (localGameState)
                        .setDelayInterval (100)
                        .setDelays (3)
                        .setTickrate (20)
                        .createTSS ();


        /**
         * Adding new dragons to the battlefield
         */
        for (int i = 0; i < DRAGON_COUNT; i++) {
            Dragon localDragon = new Dragon(UUID.randomUUID().toString());

			if (tss.populateDragon (localDragon)) {

                /* Awaken the dragon */
                new Thread (localDragon).start ();
            }

        }

        LocateRegistry.createRegistry(myPort);
        Naming.bind(myRMIAddress, broker);

        broker.registerListener (Message.ATTACK, tss);
        broker.registerListener (Message.HEAL, tss);
        broker.registerListener (Message.MOVE, tss);
        broker.registerListener (Message.LOGIN, tss);
        broker.registerListener (Message.HEARTBEAT, tss);
        broker.registerListener (Message.HANDSHAKE, serverHandler);
        broker.registerListener(Message.ATTACK, clientPlayHandler);
        broker.registerListener(Message.HEAL, clientPlayHandler);
        broker.registerListener(Message.MOVE, clientPlayHandler);

    }

    public static TrailingStateSynchronization getTSS () {
        return tss;
    }

}

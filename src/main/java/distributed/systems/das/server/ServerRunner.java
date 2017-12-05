package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Services.HeartbeatService;
import distributed.systems.das.server.Services.MessageBroker;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.Units.Dragon;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.UUID;

public class ServerRunner {

    private final static String serverID = "123";
    private static final int DRAGON_COUNT = 20;

    public static void main(String args[]) throws Exception {

        EventList eventList = new EventList();
        GameState localGameState = new GameState(1,eventList);
//        WishList wishList = new WishList(localGameState);
        HeartbeatService heartbeatService = new HeartbeatService(localGameState);
        new Thread(heartbeatService).start();

//        wishList.registerListener(localGameState);
//        wishList.registerListener(heartbeatService);

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

            if(localGameState.populateDragon(localDragon)){

                /* Awaken the dragon */
                new Thread(localDragon).start();
            }

        }

        MessageBroker broker = new MessageBroker();
        LocateRegistry.createRegistry(5001);
        Naming.bind("//localhost:5001/"+serverID, broker);

		broker.registerListener (Message.ATTACK, tss);
		broker.registerListener (Message.HEAL, tss);
		broker.registerListener (Message.MOVE, tss);
		broker.registerListener (Message.LOGIN, tss);
		broker.registerListener (Message.HEARTBEAT, tss);

    }

}

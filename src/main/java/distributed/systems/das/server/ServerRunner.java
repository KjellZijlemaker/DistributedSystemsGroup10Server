package distributed.systems.das.server;

import distributed.systems.das.server.Services.HeartbeatService;
import distributed.systems.das.server.Services.MessageBroker;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.events.EventList;
import distributed.systems.das.server.events.Message;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServerRunner {

    final static String serverID = "123";

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

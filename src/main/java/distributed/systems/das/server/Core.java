package distributed.systems.das.server;

import distributed.systems.das.server.Services.WishList;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.State.TrailingStateSynchronization;
import distributed.systems.das.server.events.EventList;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Core {

    public static void main(String args[]) throws Exception {

        EventList eventList = new EventList();
        GameState localGameState = new GameState(1,eventList);
        WishList wishList = new WishList(localGameState.getBattleField(), localGameState);
        TrailingStateSynchronization tss =
                new TrailingStateSynchronization.TSSBuilder (localGameState)
                        .setDelayInterval (100)
                        .setDelays (3)
                        .setWishList (wishList)
                        .setTickrate (20)
                        .createTSS ();

        tss.getState(1).getEventList();
        LocateRegistry.createRegistry(5001);
        Naming.rebind("//localhost:5001/wishes", wishList);
        System.out.println("test");

        while (true) {
            wishList.updateClients();
            Thread.sleep(1000);
            System.out.println(localGameState.getEventList().getEvents());
        }

    }

}

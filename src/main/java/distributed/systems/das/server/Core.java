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
        LocateRegistry.createRegistry(5001);
        GameState localGameState = new GameState(Integer.toUnsignedLong(1),eventList);
        WishList wishList = new WishList();
        TrailingStateSynchronization tss =
                new TrailingStateSynchronization.TSSBuilder (localGameState)
                        .setDelayInterval (100)
                        .setDelays (3)
                        .setWishList (wishList)
                        .setTickrate (20)
                        .createTSS ();

        Naming.rebind("//localhost:5001/wishes", wishList);
        wishList.run();



//        Naming.rebind("//:5001/battlefield", BattleField);

//        UpdateGameState sii = new UpdateGameState(wishList);
//        Naming.rebind("//:5001/auth", sii);
//        System.out.println("gameserver registered and ready");
//        Thread updateThread = new Thread(sii, "gameserver");
//        updateThread.start();


        /*
         * Server will wait for new connections from other servers, send local wishlist and check remote wishlist.
         *
         * Also, we must decide how all nodes should behave. Maybe we should create one coordinator which will
         * compare all TSS and handles TSS updates, listens to the other server nodes and send updates back to
         * these server nodes.
         * If so, we should add a new type of node which will only listen to wishlists per server tick and
         * send them back when checked
         */

//        Vector serverClients = new Vector();
//        List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> remoteWishList = new java.util.ArrayList<>();
//
//        UpdateWishList sii1 = new UpdateWishList(serverClients, wishList, remoteWishList);
//        Naming.rebind("//:5001/ServerUpdateReceiver1", sii1);
//        System.out.println("ServerUpdateReceiver1 registered and ready");
//        Thread updateThread1 = new Thread(sii1, "ServerUpdateReceiver1");
//        updateThread1.start();


//        while (true) {
//
//            // ServerTick
//            Thread.sleep(200);
//
//            // Get remote wishlist
//            List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> t1 = sii1.getRemoteWishList();
//
//            for (int i = 0; i < t1.size(); i++) {
//                String serverID = t1.get(i).getKey();
//                Map.Entry<String, String> listofValues = t1.get(i).getValue();
//            }
//
//            // Do something with TSS
//
//
//        }

    }

}

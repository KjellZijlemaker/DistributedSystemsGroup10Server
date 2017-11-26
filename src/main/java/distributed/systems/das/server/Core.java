package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.RMIGameStateInfo;
import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.Services.UpdateGameState;
import distributed.systems.das.server.Services.UpdateWishList;
import distributed.systems.das.server.events.EventList;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.*;

public class Core {

    public static void main(String args[]) throws Exception {
        Vector clients = new Vector();
        Vector serverClients = new Vector();
        java.util.List<java.util.Map.Entry<String,String>> wishList= new java.util.ArrayList<>(); //Wishlist for all moves or requests players
        List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> remoteWishList = new java.util.ArrayList<>();

        LocateRegistry.createRegistry(5001);
        UpdateGameState sii = new UpdateGameState(clients, wishList);
        Naming.rebind("//:5001/gameserver", (RMIGameStateInfo) sii);
        System.out.println("gameserver registered and ready");
        Thread updateThread = new Thread(sii, "gameserver");
        updateThread.start();


        /**
         * Server will wait for new connections from other servers, send local wishlist and check remote wishlist.
         *
         * Also, we must decide how all nodes should behave. Maybe we should create one coordinator which will
         * compare all TSS and handles TSS updates, listens to the other server nodes and send updates back to
         * these server nodes.
         * If so, we should add a new type of node which will only listen to wishlists per server tick and
         * send them back when checked
         */

        UpdateWishList sii1 = new UpdateWishList(serverClients, wishList, remoteWishList);
        Naming.rebind("//:5001/ServerUpdateReceiver1", sii);
        System.out.println("ServerUpdateReceiver1 registered and ready");
        Thread updateThread1 = new Thread(sii1, "ServerUpdateReceiver1");
        updateThread1.start();


        while(true){

            // ServerTick
            Thread.sleep(200);

            // Get remote wishlist
            List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> t1 = sii1.getRemoteWishList();

            for (int i = 0; i < t1.size(); i++) {
                String serverID = t1.get(i).getKey();
                Map.Entry<String, String> listofValues = t1.get(i).getValue();
            }

            // Do something with TSS


        }

    }

}

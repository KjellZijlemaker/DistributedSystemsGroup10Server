package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.RMIGameStateInfo;
import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.Services.UpdateGameState;
import distributed.systems.das.server.events.EventList;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Core {

    public static void main(String args[]) throws Exception {
        Vector clients = new Vector();
        java.util.List<java.util.Map.Entry<String,String>> wishList= new java.util.ArrayList<>(); //Wishlist for all moves or requests players

        LocateRegistry.createRegistry(5001);
        UpdateGameState sii = new UpdateGameState(clients, wishList);
        Naming.rebind("//:5001/gameserver", (RMIGameStateInfo) sii);
        System.out.println("gameserver registered and ready");
        Thread updateThread = new Thread(sii, "gameserver");
        updateThread.start();


        /**
         * When server has received all wishlists from other servers, empty wishlist
         *
         */

//        UpdateGameState sii1 = new UpdateGameState(clients, wishList);
//        Naming.rebind("//:5001/ServerUpdateReceiver1", sii);
//        System.out.println("ServerUpdateReceiver1 registered and ready");
//        Thread updateThread1 = new Thread(sii, "ServerUpdateReceiver1");
//        updateThread1.start();



    }

}

package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.Interfaces.RMIServerEventListener;
import distributed.systems.das.server.Interfaces.RMIServerEventUpdate;
import distributed.systems.das.server.events.GameState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class UpdateWishList extends UnicastRemoteObject implements RMIServerEventListener, Runnable {
    private Vector serverClients;
    private List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> remoteWishList;
    private java.util.List<java.util.Map.Entry<String,String>> wishList;

    public UpdateWishList(Vector serverClients, java.util.List<java.util.Map.Entry<String, String>> wishList,
                          List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> remoteWishList) throws RemoteException {
        this.serverClients = serverClients;
        this.wishList = wishList;
        this.remoteWishList = remoteWishList;
    }

    @Override
    public void register(RMIServerEventUpdate o, String serverID) throws RemoteException {
        if (serverClients.contains(o)) return;

        serverClients.add(o);
        System.out.println("Registered new server " + o + serverID);

    }

    @Override
    public void unregister(RMIServerEventUpdate o) throws RemoteException {
        if (serverClients.remove(o)) {
            System.out.println("Unregistered Server " + o);

        } else {
            System.out.println("unregister: Server " + o + "wasn't registered.");
        }
    }

    @Override
    public String receiveWishList(RMIGameStateUpdate o, List<Map.Entry<String, String>> wishList, String serverID) throws RemoteException {
        if(remoteWishList.add(new AbstractMap.SimpleEntry<>(serverID, (Map.Entry<String, String>) wishList))){
            return "OK";
        }
        return null;
    }

    @Override
    public void run() {
        while (true) {

            // For each server tick, send update to all servers
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Enumeration e = serverClients.elements(); e.hasMoreElements(); ) {

                RMIServerEventUpdate serverClient = (RMIServerEventUpdate) e.nextElement();
                try {
                    serverClient.update(this.wishList);
                } catch (RemoteException ex) {
                    System.out.println("update to server " + serverClient + " failed.");
                    try {
                        unregister(serverClient);
                        System.out.println("Remote list is now at size: " + this.remoteWishList.size());
                    } catch (RemoteException rex) {
                        System.err.println("Big trouble.");
                        rex.printStackTrace();
                        System.exit(3);
                    }
                }
            }


            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException iex) {
            }
        }
    }
    public List<AbstractMap.SimpleEntry<String, Map.Entry<String, String>>> getRemoteWishList(){
        return this.remoteWishList;
    }
}

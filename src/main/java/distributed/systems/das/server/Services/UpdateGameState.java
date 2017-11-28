package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.RMIGameStateInfo;
import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.GameState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class UpdateGameState extends UnicastRemoteObject implements RMIGameStateInfo, Runnable {

    private java.util.List<java.util.Map.Entry<String,String>> wishList;
    private Vector clients;

    /**
     * Wishlist will contain the userID and the move/attack or health request
     * @param clients
     * @param wishList
     * @throws RemoteException
     */
    public UpdateGameState(Vector clients, java.util.List<java.util.Map.Entry<String,String>> wishList) throws RemoteException {
        this.clients = clients;
        this.wishList = wishList;

    }

    @Override
    public void register(RMIGameStateUpdate o, String userID, String password) throws RemoteException {
        if (clients.contains(o)) return;

        clients.add(o);
        System.out.println("Registered new client " + o + userID);
        GameState.setPlayerCount(clients.size());
    }

    @Override
    public void unregister(RMIGameStateUpdate o) throws RemoteException {
        if (clients.remove(o)) {
            System.out.println("Unregistered client " + o);
            GameState.setPlayerCount(clients.size());

            for (int i = 0; i < wishList.size(); i++) {
                System.out.println(wishList.get(i));
            }
        } else {
            System.out.println("unregister: client " + o + "wasn't registered.");
        }
    }

    @Override
    public synchronized String moveParameter(String move, RMIGameStateUpdate o, String userID) throws RemoteException {
        if(wishList.add(new AbstractMap.SimpleEntry<>(userID, move))){
            return "OK";
        }
        return null;
    }

    @Override
    public void run() {
        while (true) {
            for (Enumeration e = clients.elements(); e.hasMoreElements() ;) {

                RMIGameStateUpdate client = (RMIGameStateUpdate) e.nextElement();
                try {
                    client.update(GameState.getRunningState(), GameState.getPlayerCount());
                } catch (RemoteException ex) {
                    System.out.println("update to client " + client + " failed.");
                    try {
                        unregister(client);
                        System.out.println("list is now at size: " + this.wishList.size());
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
}

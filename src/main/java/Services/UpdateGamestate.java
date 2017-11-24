package Services;

import Interfaces.RMIGameStateInfo;
import Interfaces.RMIGameStateUpdate;
import events.GameState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.LinkedTransferQueue;

/**
 * https://www.javaworld.com/article/2077454/soa/an-in-depth-look-at-rmi-callbacks-4-20-99.html
 */

public class UpdateGamestate extends UnicastRemoteObject implements RMIGameStateInfo, Runnable {
    private Vector clients;

    // Get clients
    public UpdateGamestate(Vector clients) throws RemoteException {
        this.clients = clients;
    }


    /**
     * For each client, send updates and unregister them when they disconnect
     */
    public void run() {

        while (true) {

            for (Enumeration e = clients.elements(); e.hasMoreElements() ;) {
                    RMIGameStateUpdate client = (RMIGameStateUpdate) e.nextElement();
                    try {
                        client.update(GameState.getRunningState(), GameState.getPlayerCount());
                    }
                    catch (RemoteException ex) {
                        System.out.println("update to client " + client + " failed.");
                        try {
                            unregister(client);
                        }
                        catch (RemoteException rex) {
                            System.err.println("Big trouble.");
                            rex.printStackTrace();
                            System.exit(3);
                        }
                    }
                }


            // sleep for two seconds
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException iex) { }
        }
    }

    @Override
    public synchronized void register(RMIGameStateUpdate o) throws RemoteException {
        if (!(clients.contains(o))) {
            clients.add(o);
            System.out.println("Registered new client " + o);
            GameState.setPlayerCount(clients.size());
        }
    }

    @Override
    public synchronized void unregister(RMIGameStateUpdate o) throws RemoteException {
        if (clients.remove(o)) {
            System.out.println("Unregistered client " + o);
            GameState.setPlayerCount(clients.size());
        } else {
            System.out.println("unregister: client " + o + "wasn't registered.");
        }
    }

    /**
     * Move updates from client
     * @param move
     * @return
     * @throws RemoteException
     */
    @Override
    public String moveParameter(String move) throws RemoteException {
        System.out.println("Move: " + move + " to queue");
        return "OK";
    }
}

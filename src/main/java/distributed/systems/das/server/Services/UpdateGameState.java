package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.RMIGameStateInfo;
import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.events.GameState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UpdateGameState extends UnicastRemoteObject implements RMIGameStateInfo, Runnable {

    private List<RMIGameStateUpdate> clients;

    public UpdateGameState(List<RMIGameStateUpdate> clients) throws RemoteException {
        this.clients = clients;
    }

    @Override
    public void register(RMIGameStateUpdate o) throws RemoteException {
        if (clients.contains(o)) return;

        clients.add(o);
        System.out.println("Registered new client " + o);
        GameState.setPlayerCount(clients.size());
    }

    @Override
    public void unregister(RMIGameStateUpdate o) throws RemoteException {
        if (clients.remove(o)) {
            System.out.println("Unregistered client " + o);
            GameState.setPlayerCount(clients.size());
        } else {
            System.out.println("unregister: client " + o + "wasn't registered.");
        }
    }

    @Override
    public String moveParameter(String move) throws RemoteException {
        System.out.println("Move: " + move + " to queue");
        return "OK";
    }

    @Override
    public void run() {
        while (true) {
            for (RMIGameStateUpdate client : clients) {
                try {
                    client.update(GameState.getRunningState(), GameState.getPlayerCount());
                } catch (RemoteException ex) {
                    System.out.println("update to client " + client + " failed.");
                    try {
                        unregister(client);
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

package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.RMIGameStateInfo;
import distributed.systems.das.server.Interfaces.RMIGameStateUpdate;
import distributed.systems.das.server.Services.UpdateGameState;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class Core {

    public static void main(String args[]) throws Exception {
        List<RMIGameStateUpdate> clients = new ArrayList<>();

        LocateRegistry.createRegistry(5001);
        UpdateGameState sii = new UpdateGameState(clients);
        Naming.rebind("//:5001/gameserver", (RMIGameStateInfo) sii);
        System.out.println("gameserver registered and ready");
        Thread updateThread = new Thread(sii, "gameserver");
        updateThread.start();

        UpdateGameState sii1 = new UpdateGameState(clients);
        Naming.rebind("//:5001/ServerUpdateReceiver1", sii);
        System.out.println("ServerUpdateReceiver1 registered and ready");
        Thread updateThread1 = new Thread(sii, "ServerUpdateReceiver1");
        updateThread1.start();
    }

}

package distributed.systems.das.server;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.Services.Callback;
import distributed.systems.das.server.Services.HeartbeatService;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.Units.Player;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.*;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Here we can call the remote objects through the implemented interfaces. Please look in the method body for more
 * explanation
 */
public class ClientRunner extends UnicastRemoteObject implements IMessageReceivedHandler {

    static final Logger Log = LoggerFactory.getLogger(ClientRunner.class);

    protected ClientRunner() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        long currentTime = System.currentTimeMillis();
        long end = currentTime + 10000000; // Initial ending time for testing with single player
        double botTime;

        String playerID = UUID.randomUUID().toString();


        // Create list with servers
        java.util.List<Map.Entry<String, Integer>> servers = new java.util.ArrayList<>();
        servers.add(new java.util.AbstractMap.SimpleEntry<>("localhost", 5454));

        Map.Entry<String, Integer> randomServer = servers.get(new Random().nextInt(servers.size()));

        /**
         * Set bot ID and total time before disconnecting bot from server
         */
        if ((args != null) && (args.length > 0)) {
            //for Bot (GTASimulator), uses 2 arguments: 1. playerID 2. runtime/lifespan
            playerID = args[0];
            botTime = Double.parseDouble(args[1]) * 1000; // From seconds to MS
            end = (long) (currentTime + botTime);
        }

        Unit localPlayer = new Player(10, 10, playerID);


        Callback updateClient = new Callback();

        try {

            ClientRunner runner = new ClientRunner();
            String serverID = "123";
            Registry remoteRegistry = LocateRegistry.getRegistry("localhost", 5001);
            IMessageReceivedHandler server = (IMessageReceivedHandler) remoteRegistry.lookup(serverID);
            remoteRegistry.bind(playerID, runner);

            Message m = new Message(0, System.currentTimeMillis(), playerID, Message.LOGIN);
            server.onMessageReceived(m);
            System.out.println(Arrays.toString(remoteRegistry.list()));
            m = new Message(1, System.currentTimeMillis(), playerID, Message.MOVE);
            m.body.put("x", 1);
            m.body.put("y", 1);
            server.onMessageReceived(m);


            /**
             * Start heartbeat thread
             */
            String finalPlayerID = playerID;
            new Thread(() -> {
                try {
                    runner.startHeartbeat(server, finalPlayerID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


            /**
             * Getting updates from server
             */
            Callback callback = new Callback();
            while (true){
                Thread.sleep(200);

                if(callback.getUpdate() != null){
                    System.out.println(callback.getUpdate());
                    Message callbackMessage = callback.getUpdate();
                    switch (callbackMessage.type){
                        case Message.ATTACK:
                            System.out.println("Attacked, you have: " + callbackMessage.body.get("adjustedHitpoints"));
                    }

                }

            }


            // Get values from server
//            boolean gameState = initialStateBattlefield.getValue0();
//            BattleField localBattlefield = initialStateBattlefield.getValue1();
//            localPlayer = initialStateBattlefield.getValue2(); // Update local player from server
//
//            // If game is running:
//            if (gameState) {
////                System.out.println(localBattlefield.getUnits());
//                System.out.println(localPlayer.getUnitID());
//                System.out.println(localBattlefield.getMap());
//            }
//
//            while (System.currentTimeMillis() < end) {
//                Thread.sleep(1000);
//
//                for (int i = 0; i < localBattlefield.getUnits().size(); i++) {
//                    System.out.println(localBattlefield.getUnits().get(i).getX());
//                    System.out.println(localBattlefield.getUnits().get(i).getY());
//                }
//                String returnMessage = user.registerWish(localPlayer, new Move.MoveBuilder(1)
//                        .setActor_id(localPlayer.getUnitID())
//                        .setTargetX(1)
//                        .setTargetY(1)
//                        .createEvent());
//                if (returnMessage.contains("OK")) {
//                    if(updateClient.getEventlist() != null){
//
//                        // TODO Update client movement on local board
//                    }
//                }
//            }
//            user.disconnectUser(localPlayer); // Disconnect when lifespan of bot is up

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }

    @Override
    public Message onMessageReceived(Message message) {
        Log.debug(message.toString());
        return null;
    }

    private void startHeartbeat(IMessageReceivedHandler server, String playerID) throws Exception {
        Message m = new Message(2, System.currentTimeMillis(), playerID, Message.HEARTBEAT);
        while (true) {
            server.onMessageReceived(m);
            Thread.sleep(10);
        }
    }


}

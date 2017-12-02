package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.events.EventList;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * The wishlist will get a pair of User/RMISendToUserInterface, because the RMI interface will control which users
 * have been connected/disconnected. The User class can be used for security, playerID, nickname etc
 */
public class WishList extends UnicastRemoteObject implements RMIUserInterface {
    private static final long serialVersionUID = 1L;
    static final Logger Log = LoggerFactory.getLogger(WishList.class);

    private Map<String, RMISendToUserInterface> userCallbacks = new HashMap<>();
    private List<Unit> players = new ArrayList<>();
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();
    private GameState localGameState;

    public WishList(GameState localGameState) throws RemoteException {
        super();
        this.localGameState = localGameState;
    }

    @Override
    public Triplet<Boolean, BattleField, Unit> connectUser(Unit remotePlayer, RMISendToUserInterface callback) throws RemoteException {
        if (remotePlayer.getUnitID().isEmpty()) {
            return null;
        }

        boolean success = tryPopulate(remotePlayer);
        if (!success) {
            return null;
        }

        Log.info("User connected with ID "+remotePlayer.getUnitID());
        userCallbacks.put(remotePlayer.getUnitID(), callback);
        players.add(remotePlayer);

        return Triplet.with(GameState.getRunningState(), BattleField.getBattleField(), remotePlayer);
    }

    private boolean tryPopulate(Unit remotePlayer) {
        int x, y, attempt = 0;

        do {
            x = (int) (Math.random() * BattleField.MAP_WIDTH);
            y = (int) (Math.random() * BattleField.MAP_HEIGHT);
            attempt++;
        } while (!localGameState.getBattleField().spawnUnit(remotePlayer, x, y) && attempt < 10);

        if (!localGameState.getBattleField().getUnit(x, y).getUnitID()
                .equals(remotePlayer.getUnitID())) {
            return false;
        }

        remotePlayer.setPosition(x, y);
        return true;
    }

    @Override
    public void disconnectUser(Unit player) throws RemoteException {
        players.remove(player);
        userCallbacks.remove(player.getUnitID());
        localGameState.getBattleField().removeUnit(player.getX(), player.getY());
        Log.info("User: " + player.getUnitID() + " disconnected");

    }

    public String registerWish(Unit player, Event event) throws RemoteException {
        boolean exists = players.stream().anyMatch(x -> x.getUnitID()
                .equals(player.getUnitID()));
        if (!exists) {
            Log.debug("User: " + player.getUnitID() + " is not registered");
            return "You are not registered";
        }
        localGameState.getEventList().add(event);
        listeners.forEach(x -> x.onMessageReceived(event));
        Log.debug("Event ID: " + event.getId() + " from User: " + player.getUnitID() + " received");
        return "OK";
    }

    public void updateClients() {
        List<String> brokenConnections = new ArrayList<>();
        Map<String, RMISendToUserInterface> map = Collections.synchronizedMap(userCallbacks);

        synchronized (map){
            for (String unitID : map.keySet()) { // TODO concurrent modification exception if someone adds element while we are iterating
                RMISendToUserInterface callback = userCallbacks.get(unitID);
                try {
                    callback.update(localGameState.getEventList());
                        for(int i = 0; i < localGameState.getEventList().getEvents().size(); i++){
                            if(localGameState.getEventList().getEvents().get(i).getActor_id().contains(unitID)){
                                System.out.println(localGameState.getEventList().getEvents().get(i).getActor_id() + ", " + localGameState.getEventList().getEvents().get(i).getActor_id());
                                localGameState.getEventList().getEvents().remove(i);

                            }
//                            if(events.getEvents().get(i).getActor_id() == localGameState.getEventList().getEvents().get(i).getActor_id()){
//                                System.out.println(events.getEvents().get(i).getActor_id() + ", " + localGameState.getEventList().getEvents().get(i).getActor_id());
//
//                                localGameState.getEventList().getEvents().remove(events.getEvents().get(i));
//                            }
                        }
                } catch (RemoteException ex) {
                    brokenConnections.add(unitID);

                        for (int i = 0; i < localGameState.getEventList().getEvents().size(); i++) {
                            if (localGameState.getEventList().getEvents().get(i).getActor_id().contains(unitID)) {
                                System.out.println(localGameState.getEventList().getEvents().get(i).getActor_id() + ", " + localGameState.getEventList().getEvents().get(i).getActor_id());
                                localGameState.getEventList().getEvents().remove(i);

                            }
                        }
                }
        }
        }

        for (String unitID : brokenConnections) {
            Unit remoteUser = players.stream()
                    .filter(x -> x.getUnitID()
                            .equals(unitID))
                    .findFirst().orElse(null);
            try {
                disconnectUser(remoteUser);
            } catch (RemoteException e) {
                Log.warn("Cannot disconnect User: " + unitID);
                e.printStackTrace();
            }
        }
    }

    public void registerListener(IMessageReceivedHandler handler) {
        listeners.add(handler);
    }

    public static List<Pair> getUserObjectList() {
        return Collections.emptyList();
    }

}


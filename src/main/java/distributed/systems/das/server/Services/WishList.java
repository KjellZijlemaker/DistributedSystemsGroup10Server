package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Event;
import distributed.systems.das.server.util.Log;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * The wishlist will get a pair of User/RMISendToUserInterface, because the RMI interface will control which users
 * have been connected/disconnected. The User class can be used for security, playerID, nickname etc
 */
public class WishList extends UnicastRemoteObject implements RMIUserInterface {
    private static final long serialVersionUID = 1L;
    private Map<String, RMISendToUserInterface> userCallbacks = new HashMap<>();
    private List<Unit> players = new ArrayList<>();
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();
    private BattleField battlefield;

    public WishList(BattleField battleField) throws RemoteException {
        super();
        this.battlefield = battleField;
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
        } while (!battlefield.spawnUnit(remotePlayer, x, y) && attempt < 10);

        if (!battlefield.getUnit(x, y).getUnitID()
                .equals(remotePlayer.getUnitID())) {
            return false;
        }

        System.out.println("X is:" + x + "and y is: " + y);
        remotePlayer.setPosition(x, y);
        return true;
    }

    @Override
    public void disconnectUser(Unit player) throws RemoteException {
        players.remove(player);
        userCallbacks.remove(player.getUnitID());
        battlefield.removeUnit(player.getX(), player.getY());
        System.out.println("disconnected");
//      Log.serverUpdate("User: " + player.getUnitID() + " disconnected");

    }

    public String registerWish(Unit player, Event event) throws RemoteException {
        boolean exists = players.stream().anyMatch(x -> x.getUnitID()
                .equals(player.getUnitID()));
        if (!exists) {
//            Log.serverError("User: " + player.getUnitID() + " is not registered");
            return "You are not registered";
        }
        listeners.forEach(x -> x.onMessageReceived(event));
//        Log.serverUpdate("Event ID: " + event.getId() + " from User: " + player.getUnitID() + " received");
        return "OK";
    }

    public void updateClients() {
        List<String> brokenConnections = new ArrayList<>();
        for (String unitID : userCallbacks.keySet()) { // TODO concurrent modification exception if someone adds element while we are iterating
            RMISendToUserInterface callback = userCallbacks.get(unitID);
            try {
                callback.update("Update to client");
            } catch (RemoteException ex) {
                brokenConnections.add(unitID);
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
                Log.serverError("Cannot disconnect User: " + unitID);
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


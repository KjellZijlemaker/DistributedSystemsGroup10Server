package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The wishlist will get a pair of User/RMISendToUserInterface, because the RMI interface will control which users
 * have been connected/disconnected. The User class can be used for security, playerID, nickname etc
 */
public class Wishlist extends UnicastRemoteObject {
    private static final long serialVersionUID = 1L;
    static final Logger Log = LoggerFactory.getLogger(Wishlist.class);

    private Map<String, RMISendToUserInterface> userCallbacks = new ConcurrentHashMap<>();
    private List<Unit> players = new ArrayList<>();
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();
    private distributed.systems.das.server.State.GameState localGameState;

    public Wishlist(distributed.systems.das.server.State.GameState localGameState) throws RemoteException {
        super();
        this.localGameState = localGameState;
    }

    public String registerWish(Unit player, Event event) throws RemoteException {
        boolean exists = players.stream().anyMatch(x -> x.getUnitID()
                .equals(player.getUnitID()));
        if (!exists) {
            Log.debug("User: " + player.getUnitID() + " is not registered");
            return "You are not registered";
        }
        localGameState.getEventList().add(event);
        listeners.forEach(x -> {
//            try {
//                x.onMessageReceived(event);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        });
        Log.debug("Event ID: " + event.getId() + " from User: " + player.getUnitID() + " received");

        updateClients(event);
        return "OK";
    }

    public void updateClients(Event event) {
        List<String> brokenConnections = new ArrayList<>();

        for (String unitID : userCallbacks.keySet()) { // TODO concurrent modification exception if someone adds element while we are iterating
            RMISendToUserInterface callback = userCallbacks.get(unitID);
            try {
                callback.update(event);
            } catch (RemoteException ex) {
                brokenConnections.add(unitID);
            } finally {
//                for (int i = 0; i < localGameState.getEventList().getEvents().size(); i++) {
//                    if (localGameState.getEventList().getEvents().get(i).getActor_id().contains(unitID)) {
////                        System.out.println(localGameState.getEventList().getEvents().get(i).getActor_id() + ", " + localGameState.getEventList().getEvents().get(i).getActor_id());
//                        localGameState.getEventList().getEvents().remove(i);
//                        i--;
//                    }
//                }
            }
        }

        for (String unitID : brokenConnections) {
            Unit remoteUser = players.stream()
                    .filter(x -> x.getUnitID()
                            .equals(unitID))
                    .findFirst().orElse(null);
//            try {
//                disconnectUser(remoteUser);
//            } catch (RemoteException e) {
//                Log.warn("Cannot disconnect User: " + unitID);
//                e.printStackTrace();
//            }
        }
    }

    public void registerListener(IMessageReceivedHandler handler) {
        listeners.add(handler);
    }

}


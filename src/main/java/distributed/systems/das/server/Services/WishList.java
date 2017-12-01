package distributed.systems.das.server.Services;

import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.Units.Player;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Event;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * The wishlist will get a pair of User/RMISendToUserInterface, because the RMI interface will control which users
 * have been connected/disconnected. The User class can be used for security, playerID, nickname etc
 */
public class WishList extends UnicastRemoteObject implements RMIUserInterface, Runnable {
    private static final long serialVersionUID = 1L;
    private static List<Triplet> userObjectList = new ArrayList<>();
    private static ArrayList<Unit> players = new ArrayList<>();
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();
    private BattleField battlefield;

    public WishList(BattleField battleField) throws RemoteException {
        super();
        this.battlefield = battleField;
    }

    @Override
    public Triplet<Boolean, BattleField, Player> connectUser(Triplet userObject) throws RemoteException {
        User newUser = (User) userObject.getValue0();
        Player remotePlayer = (Player) userObject.getValue2();

        if (newUser.getUserID().isEmpty()) {
            return null;
        }

        System.out.println("Player: " + newUser.getUserID() + " connected");
        if (userObjectList.add(userObject)) {
            this.battlefield = BattleField.getBattleField();

            /* Once again, pick a random spot */

            int x, y, attempt = 0;

            do {
                x = (int) (Math.random() * BattleField.MAP_WIDTH);
                y = (int) (Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (battlefield.getUnit(x, y) != null && attempt < 10);

            // If we didn't find an empty spot, we won't add a new player
            if (battlefield.getUnit(x, y) != null) {
                return null;
            }
            final int finalX = x;
            final int finalY = y;
            System.out.println("X is:" + finalX + "and y is: " + finalY);

            remotePlayer.setPosition(finalX, finalY);
            remotePlayer.setPlayerID(newUser.getUserID());
            players.add(remotePlayer);

            // Send back initial update to player
            Triplet<Boolean, BattleField, Player> s = Triplet.with(GameState.getRunningState(), BattleField.getBattleField(), remotePlayer);
            battlefield.setUnits(players);
            System.out.println(battlefield.getUnits());
            return s;
        }
        return null;
    }

    @Override
    public void disconnectUser(Triplet userObject) throws RemoteException {
        User user = (User) userObject.getValue0();
        Player player = (Player) userObject.getValue2();
        if (userObjectList.remove(userObject) && players.remove(player)) {
            battlefield.setUnits(players);
            System.out.println("User: " + user.getUserID() + " removed");
            System.out.println(battlefield.getUnits());
        } else {
            System.out.println("Failed to disconnect");
        }

    }


    public String registerWish(Pair userObject, Event event) throws RemoteException {
        if (!userObjectList.contains(userObject)) {
            return "You are not registered";
        }
        listeners.forEach(x -> x.onMessageReceived(event));
        return "OK";
    }


    /**
     * Send back message
     */
    @Override
    public void run() {
        while (true) {
            for (int i = 0; i < userObjectList.size(); i++) {

                RMISendToUserInterface client = (RMISendToUserInterface) userObjectList.get(i).getValue1();
                try {
                    client.update("Update to client");
                } catch (RemoteException ex) {
                    try {
                        disconnectUser(userObjectList.get(i));
                        //System.out.println("list is now at size: " + this.wishList.size());
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

    public void registerListener(IMessageReceivedHandler handler) {
        listeners.add(handler);
    }

    public static List<Triplet> getUserObjectList() {
        return userObjectList;
    }
}


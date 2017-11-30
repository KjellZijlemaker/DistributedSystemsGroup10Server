package distributed.systems.das.server.Services;

import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMISendToUserInterface;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.events.Event;
import org.javatuples.Pair;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * The wishlist will get a pair of User/RMISendToUserInterface, because the RMI interface will control which users
 * have been connected/disconnected. The User class can be used for security, playerID, nickname etc
 */
public class WishList extends UnicastRemoteObject implements RMIUserInterface, Runnable {
    private static final long serialVersionUID = 1L;
    private List<Pair> userObjectList = new ArrayList<>();
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();


    public WishList() throws RemoteException {
        super();
    }

    @Override
    public String connectUser(Pair userObject) throws RemoteException {
        User newUser = (User) userObject.getValue0();

        if(newUser.getUserID().isEmpty()) {
            return null;
        }

        System.out.println("Player: " + newUser.getUserID() + " connected");
        if (userObjectList.add(userObject)) {
            return "OK";
        }
        return "Already connected";
    }

    @Override
    public void disconnectUser(Pair userObject) throws RemoteException {
        User user = (User) userObject.getValue0();
        if(userObjectList.remove(userObject)){
            System.out.println("User: " + user.getUserID() + " removed");
        }
        else{
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
            for(int i = 0; i < userObjectList.size(); i++){

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

}

package distributed.systems.das.server.Services;

import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Interfaces.RMIUserInterface;
import distributed.systems.das.server.events.Event;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class WishList extends UnicastRemoteObject implements RMIUserInterface {
    private static final long serialVersionUID = 1L;
    private List<User> userList;
    private List<IMessageReceivedHandler> listeners = new ArrayList<>();

    public WishList() throws RemoteException {
        super();
        this.userList = new ArrayList<>();
    }

    @Override
    public String connectUser(User user) throws RemoteException {
        if (user.getUserID().isEmpty()) {
            return null;
        }

        System.out.println("Player: " + user.getUserID() + " connected");
        if (userList.add(user)) {
            return "OK";
        }
        return "Already connected";
    }

    public String registerWish(User user, Event event) throws RemoteException {
        if (!userList.contains(user)) {
            return "You are not registered";
        }
        listeners.forEach(x -> x.onMessageReceived(event));
        return "OK";
    }

    public void registerListener(IMessageReceivedHandler handler) {
        listeners.add(handler);
    }

}

package Services;

import Beans.User;
import Interfaces.RMIUserInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The service will handle the logic which the server must execute. Note that the objects of the interface must be
 * implemented
 */
public class UserStore extends UnicastRemoteObject implements RMIUserInterface {
    private static final long serialVersionUID = 1L;
    private static final String serverID = UUID.randomUUID().toString();
    private List<User> userList;

    public UserStore() throws RemoteException {
        super();
        this.userList = new ArrayList<>();
    }

//    //The client sends a Beans.User object with the isbn information on it (note: it could be a string with the isbn too)
//    //With this method the server searches in the List userList for any user that has that isbn and returns the whole object
//    @Override
//    public Beans.User findBook(Beans.User user) throws RemoteException {
//        Predicate<Beans.User> predicate = x-> x.getIsbn().equals(user.getIsbn());
//        return userList.stream().filter(predicate).findFirst().get();
//
//    }

    @Override
    public String connectUser(User user) throws RemoteException {
        if(user.getUserID() != ""){
            System.out.println("Player: " + user.getUserID() + " connected");
        }
        if(this.userList.add(user)){
            return this.serverID;
        }

        return null;
    }


}


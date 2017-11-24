package distributed.systems.das;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserStore extends UnicastRemoteObject implements RMIInterface {
    private static final long serialVersionUID = 1L;
    private static final String serverID = UUID.randomUUID().toString();
    private List<User> userList;

    protected UserStore() throws RemoteException {
        super();
        this.userList = new ArrayList<>();
    }

//    //The client sends a User object with the isbn information on it (note: it could be a string with the isbn too)
//    //With this method the server searches in the List userList for any user that has that isbn and returns the whole object
//    @Override
//    public User findBook(User user) throws RemoteException {
//        Predicate<User> predicate = x-> x.getIsbn().equals(user.getIsbn());
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


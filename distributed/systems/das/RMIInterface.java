package distributed.systems.das;

import distributed.systems.das.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    public String connectUser(User user) throws RemoteException;
}

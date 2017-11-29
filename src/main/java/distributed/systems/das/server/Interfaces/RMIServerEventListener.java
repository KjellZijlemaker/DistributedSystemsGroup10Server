package distributed.systems.das.server.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerEventListener extends Remote {
    void register(RMIServerEventUpdate o, String serverID) throws RemoteException;
    void unregister(RMIServerEventUpdate o) throws RemoteException;
    String receiveWishList(RMIGameStateUpdate o, java.util.List<java.util.Map.Entry<String,String>> wishList, String serverID) throws RemoteException;
}

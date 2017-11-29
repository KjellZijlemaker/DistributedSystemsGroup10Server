package distributed.systems.das.server.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerEventUpdate extends Remote{
    void update(java.util.List<java.util.Map.Entry<String,String>> wishList) throws RemoteException;
}

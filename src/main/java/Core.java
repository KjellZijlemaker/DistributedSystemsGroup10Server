import Interfaces.RMIGameStateInfo;
import Services.UpdateGamestate;
import Services.UserStore;
import events.EventList;
import events.GameState;

import java.io.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

/**
 * A server can run several services. Each can have a different Service class and each class will have a different
 * interface to implement. When working on the code, please do this for readability of the code
 */
public class Core  {
    private static final String POISON_PILL = "POISON_PILL";

	public static void main(String args[]) {

//		try {
//			Naming.rebind("//localhost/userRegistry", new UserStore());
//			System.err.println("user registry ready");
//
//		} catch (Exception e) {
//			System.err.println("Server exception: " + e.getMessage());
//		}

		try {
			Vector clients = new Vector(); //Store all clients

			//set policy for allowing creation of sockets
			System.setProperty("java.security.policy",
					"src/main/java/Security/policy.all");
			System.setSecurityManager(new RMISecurityManager());

			/**
			 * Listener for receiving updates/positions from clients and sending back updates/positions to the client
			 * For now it will send back gamestate only
			 */
			try {
				LocateRegistry.createRegistry(5001);
				UpdateGamestate sii = new UpdateGamestate(clients);
				Naming.rebind("//:5001/gameserver", sii);
				System.out.println("gameserver registered and ready");
				Thread updateThread = new Thread(sii, "gameserver");
				updateThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}


			/**
			 * Listener for sending positions and updates to other servers
			 */
			try {
				LocateRegistry.createRegistry(5002);
				UpdateGamestate sii = new UpdateGamestate(clients);
				Naming.rebind("//:5002/ServerUpdateReceiver1", sii);
				System.out.println("ServerUpdateReceiver1 registered and ready");
				Thread updateThread = new Thread(sii, "ServerUpdateReceiver1");
				updateThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.err.println("Server exception: " + e.getMessage());
		}

	}

}

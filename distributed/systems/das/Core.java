package distributed.systems.das;

import java.io.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Core extends UnicastRemoteObject  {
	public Core() throws RemoteException {
		super();
	}
    private static final String POISON_PILL = "POISON_PILL";

	public static void main(String args[]) {

		try {
			Naming.rebind("//localhost/userRegistry", new UserStore());
			System.err.println("user registry ready");

		} catch (Exception e) {
			System.err.println("Server exception: " + e.getMessage());
		}

		try {
			Naming.rebind("//localhost/runGame", new UserStore());
			System.err.println("game logic ready");

		} catch (Exception e) {
			System.err.println("Server exception: " + e.getMessage());
		}

	}




    private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public static Process start() throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = Core.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);

        return builder.start();
    }

}

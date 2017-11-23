package distributed.systems.das;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Core {
    private static final String POISON_PILL = "POISON_PILL";

    public static void main(String[] args) throws Exception {

        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 5454));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(128);

        while (true) {

            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    register(selector, serverSocket);
                }

                if (key.isReadable()) {
                    answerWithEcho(buffer, key);
                }
                iter.remove();
            }
        }
    }

    private static void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws Exception {
        SocketChannel client = (SocketChannel) key.channel();
        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            key.cancel();
            client.close();
            return;
        }

        // Get object over network and deserialize
        ByteArrayInputStream bIs = new ByteArrayInputStream(buffer.array());
        ObjectInputStream is = new ObjectInputStream(bIs);
        Kuku kuku = (Kuku) is.readObject();
        System.out.println("UUID: " + kuku.getA() + " connected");

        kuku.setA("OK"); // Set new value for A

        // Send it back to the client
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOutputStream);
        out.writeObject(kuku);
        out.flush();
        out.close();

        client.write(ByteBuffer.wrap(byteOutputStream.toByteArray()));
        buffer.clear();
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


    public void runServer() throws IOException, ClassNotFoundException{

    }

}

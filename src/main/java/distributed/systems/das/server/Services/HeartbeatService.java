package distributed.systems.das.server.Services;

import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.State.GameState;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HeartbeatService implements IMessageReceivedHandler, Runnable {
    static final Logger Log = LoggerFactory.getLogger(HeartbeatService.class);
    private final int COUNTER = 10;
    private final int WAIT_TIME = 100;
    private Map<String, Integer> monitoredEndpoints = new ConcurrentHashMap<>();
    private GameState gameState;

    public HeartbeatService(GameState localGameState) {
        this.gameState = localGameState;
    }

    // Client registers server id and checks if server responds
    public void updateEndpoint(String id) {
        monitoredEndpoints.put(id, COUNTER);
    }

    // Server receives heartbeats from client
    @Override
    public Message onMessageReceived(Message message) {
        String actorID = message.actorID;
        updateEndpoint(actorID);
        return null;
    }

    @Override
    public void run() {
        try {
            while (true) {
                checkHeartbeat();
                sendHeartbeat();
                Thread.sleep(WAIT_TIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkHeartbeat() {
        Set<String> deadPlayers = monitoredEndpoints.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .keySet();
        removeDeadPlayers(deadPlayers);

        monitoredEndpoints.forEach((key, value) -> monitoredEndpoints.put(key, value - 1));
    }

    private void removeDeadPlayers(Set<String> deadPlayers) {
        deadPlayers.forEach(p -> {
            Log.debug("removeDeadPlayer " + p);
            gameState.disconnectUser(p);
        });
    }

    private void sendHeartbeat() {
        monitoredEndpoints.entrySet().stream();
    }
}

package distributed.systems.das.server.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable, Comparable<Message> {

    public static final int ATTACK = 1;
    public static final int HEAL = 2;
    public static final int MOVE = 3;
    public static final int HEARTBEAT = 4;
    public static final int LOGIN = 5;

    public Map<String, Object> body = new HashMap<>();
    public final int id;
    public final long timestamp;
    public final String actorID;
    public final int type;

    public Message(int id, long timestamp, String actorID, int type) {
        this.id = id;
        this.timestamp = timestamp;
        this.actorID = actorID;
        this.type = type;
    }

    public Message(Message message) {
        this.type = message.type;
        this.timestamp = message.timestamp;
        this.id = message.id;
        this.actorID = message.actorID;
        body.putAll(message.body);
    }

    @Override
    public int compareTo (Message event) {
        int comparison = Long.compare (this.timestamp, event.timestamp);
        if (comparison == 0) {
            comparison = this.actorID.compareTo (event.actorID);
        }
        return comparison;
    }
}

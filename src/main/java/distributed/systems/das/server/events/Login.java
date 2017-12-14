package distributed.systems.das.server.events;

public class Login extends Event {
    /**
     * Creates an Event object
     *
     * @param id        event id
     * @param timestamp The time when the event occurs
     * @param actor_id  The id of the actor that created this event
     */
    public Login(long id, long timestamp, String actor_id) {
        super(id, timestamp, actor_id);
    }

    @Override
    public int getType() {
        return LOGIN;
    }
}

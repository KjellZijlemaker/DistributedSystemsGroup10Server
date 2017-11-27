package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.Beans.User;
import distributed.systems.das.server.events.Event;

public interface IMessageReceivedHandler {

    void onMessageReceived(Event event);
}

package distributed.systems.das.server.Interfaces;

import distributed.systems.das.server.events.Event;

public interface IMessageReceivedHandler {

    void onMessageReceived(Event event);
}

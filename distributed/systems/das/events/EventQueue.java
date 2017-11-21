package distributed.systems.das.events;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Queue for all events
 */
public class EventQueue implements Iterable<Event> {

	private PriorityBlockingQueue<Event> events = new PriorityBlockingQueue<Event> (11);

	public synchronized boolean add (Event event) {
		return this.events.add (event);
	}

	public synchronized void clear () {
		this.events.clear ();
	}

	public synchronized boolean addAll (EventQueue queue) {
		return this.events.addAll (queue.getEvents ());
	}

	public synchronized PriorityBlockingQueue<Event> getEvents () {
		return this.events;
	}

	@Override
	public Iterator<Event> iterator () {
		return this.events.iterator ();
	}

}

package distributed.systems.das.events;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Queue for all events
 */
public class EventList implements Iterable<Event>, Serializable {

	private CopyOnWriteArrayList<Event> events = new CopyOnWriteArrayList<> ();

	public synchronized boolean add (Event event) {
		return this.events.add (event);
	}

	public synchronized void add (int index, Event event) {
		this.events.add (index, event);
	}

	public synchronized void clear () {
		this.events.clear ();
	}

	public synchronized boolean addAll (EventList queue) {
		return this.events.addAll (queue.getEvents ());
	}

	public synchronized CopyOnWriteArrayList<Event> getEvents () {
		return this.events;
	}

	@Override
	public Iterator<Event> iterator () {
		return this.events.iterator ();
	}

	@Override
	public String toString () {
		return this.events.toString ();
	}

}

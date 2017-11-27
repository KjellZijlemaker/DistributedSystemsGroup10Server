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

	public synchronized boolean addAll (EventList events) {
		return this.events.addAll (events.getEvents ());
	}

	/**
	 * Returns all events
	 */
	public synchronized CopyOnWriteArrayList<Event> getEvents () {
		return this.events;
	}

	/**
	 * Returns all events in a range of time
	 * @param from oldest
	 * @param to most recent
	 */
	public synchronized CopyOnWriteArrayList<Event> getEventsByTime (long from, long to) {
		CopyOnWriteArrayList<Event> newList = new CopyOnWriteArrayList<>();
	    for (Event event : events) {
	    	long timestamp = event.getTimestamp();
	    	if (timestamp >= from && timestamp < to) {
	    		newList.add(event);
			}
		}
		return newList;
	}
	
	/**
	 * Returns the top element and removes it.
	 */
	public synchronized Event pop () {
		Event event = events.get (0);
		events.remove (0);
		return event;
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

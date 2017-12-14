package distributed.systems.das.server.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Queue for all events
 */
public class EventList implements Iterable<Message>, Serializable {

	private CopyOnWriteArrayList<Message> events;

	/**
	 * Creates new EventList
	 */
	public EventList () {
		this.events = new CopyOnWriteArrayList<> ();
	}

	/**
	 * Creates new EventList by cloning an already existing one
	 */
	public EventList (EventList toCopy) {
		this.events = new CopyOnWriteArrayList<> (toCopy.getEvents ());
	}

	public synchronized boolean add (Message event) {
		return this.events.add (event);
	}

	public synchronized void add (int index, Message event) {
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
	public synchronized CopyOnWriteArrayList<Message> getEvents () {
		return this.events;
	}

	/**
	 * Returns all events in a range of time
	 * @param from oldest
	 * @param to most recent
	 */
	public synchronized CopyOnWriteArrayList<Message> getEventsByTime (long from, long to) {
		CopyOnWriteArrayList<Message> newList = new CopyOnWriteArrayList<> ();
		for (Message event : events) {
			long timestamp = event.timestamp;
			if (timestamp >= from && timestamp < to) {
	    		newList.add(event);
			}
		}
		return newList;
	}

	public boolean isEmpty () {
		return this.events.isEmpty ();
	}
	
	/**
	 * Returns the top element and removes it.
	 */
	public synchronized Message pop () {
		Message event = events.get (0);
		events.remove (0);
		return event;
	}

	@Override
	public Iterator<Message> iterator () {
		return this.events.iterator ();
	}

	@Override
	public String toString () {
		return this.events.toString ();
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}

		if (this.getClass () != obj.getClass ()) {
			return false;
		}
		EventList list = (EventList) obj;

		if (this.events != null || list.getEvents () != null ||
				this.events.size () == list.getEvents ().size ()) {
			return false;
		}

		ArrayList<Message> x = new ArrayList<> (this.events);
		ArrayList<Message> y = new ArrayList<> (list.getEvents ());

		Collections.sort (x);
		Collections.sort (y);

		return x.equals (y);
	}
}

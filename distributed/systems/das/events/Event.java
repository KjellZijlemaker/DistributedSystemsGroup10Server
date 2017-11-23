package distributed.systems.das.events;

import java.io.Serializable;

/**
 * Super class for events
 */
public class Event implements Serializable, Comparable<Event> {

	private static long serialVersionUID = 1L;

	private long id;
	private long timestamp;
	private int actor_id;

	/**
	 * Creates an Event object
	 *
	 * @param id
	 * @param timestamp The time when the event occurs
	 * @param actor_id  The id of the actor that created this event
	 */
	public Event (long id, long timestamp, int actor_id) {
		this.id = id;
		this.timestamp = timestamp;
		this.actor_id = actor_id;
	}

	public long getId () {
		return id;
	}

	public void setId (long id) {
		this.id = id;
	}

	public long getTimestamp () {
		return timestamp;
	}

	public void setTimestamp (long timestamp) {
		this.timestamp = timestamp;
	}

	public int getActor_id () {
		return actor_id;
	}

	public void setActor_id (int actor_id) {
		this.actor_id = actor_id;
	}

	@Override
	public String toString () {
		return "Event(" + this.timestamp + ", " + this.actor_id + ")";
	}

	 /**
	 * Compares this event with the specified event for order. Returns a negative integer, zero,
	 * or a positive integer as this event is before, equal to, or later than the specified
	 * event.
	 *
	 * This first compares the timestamp, and then the ids of the two actors. Thus, an actor
	  * cannot have two actions at the same timestamp
	 *
	 * @param event The event to compare
	 */
	@Override
	public int compareTo (Event event) {
		int comparison = Long.compare (this.timestamp, event.getTimestamp ());
		if (comparison == 0) {
			comparison = Integer.compare (this.actor_id, this.actor_id);
		}
		return comparison;
	}
}

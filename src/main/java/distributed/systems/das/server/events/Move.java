package distributed.systems.das.server.events;

/**
 * Created by Jasper van Riet
 */
public class Move extends Event {

	private int targetX, targetY;

	/**
	 * Creates a Move object
	 *
	 * @param id        event id
	 * @param timestamp The time when the event occurs
	 * @param actor_id  The id of the actor that created this event
	 */
	public Move (long id, long timestamp, int actor_id,
				 int targetX, int targetY) {
		super (id, timestamp, actor_id);
		this.targetX = targetX;
		this.targetY = targetY;
	}

	@Override
	public int getType () {
		return MOVE;
	}

	public int getTargetX () {
		return targetX;
	}

	public void setTargetX (int targetX) {
		this.targetX = targetX;
	}

	public int getTargetY () {
		return targetY;
	}

	public void setTargetY (int targetY) {
		this.targetY = targetY;
	}

	public static class MoveBuilder {

		private int targetX, targetY;
		private long id;
		private long timestamp;
		private int actor_id;

		public MoveBuilder (long id) {
			this.id = id;
		}

		public int getTargetX () {
			return targetX;
		}

		public void setTargetX (int targetX) {
			this.targetX = targetX;
		}

		public int getTargetY () {
			return targetY;
		}

		public void setTargetY (int targetY) {
			this.targetY = targetY;
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

		public Move createEvent () {
			return new Move (id, timestamp, actor_id, targetX, targetY);
		}
	}
}

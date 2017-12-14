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
	private Move (long id, long timestamp, String actor_id,
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
		private String actor_id;

		public MoveBuilder (long id) {
			this.id = id;
		}

		public int getTargetX () {
			return targetX;
		}

		public MoveBuilder setTargetX (int targetX) {
			this.targetX = targetX;
			return this;
		}

		public int getTargetY () {
			return targetY;
		}

		public MoveBuilder setTargetY (int targetY) {
			this.targetY = targetY;
			return this;
		}

		public long getId () {
			return id;
		}

		public MoveBuilder setId (long id) {
			this.id = id;
			return this;
		}

		public long getTimestamp () {
			return timestamp;
		}

		public MoveBuilder setTimestamp (long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public String getActor_id () {
			return actor_id;
		}

		public MoveBuilder setActor_id (String actor_id) {
			this.actor_id = actor_id;
			return this;
		}

		public Move createEvent () {
			return new Move(id, timestamp, actor_id, targetX, targetY);
		}
	}
}

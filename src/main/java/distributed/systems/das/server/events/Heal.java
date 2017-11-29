package distributed.systems.das.server.events;

public class Heal extends Event {

	private int targetX, targetY;
	private int amount;

	/**
	 * Creates an Event object
	 *
	 * @param id        event id
	 * @param timestamp The time when the event occurs
	 * @param actor_id  The id of the actor that created this event
	 */
	private Heal (long id, long timestamp, int actor_id,
				  int targetX, int targetY, int amount) {
		super (id, timestamp, actor_id);
		this.targetX = targetX;
		this.targetY = targetY;
		this.amount = amount;
	}

	@Override
	public int getType () {
		return HEAL;
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

	public int getAmount () {
		return amount;
	}

	public void setAmount (int amount) {
		this.amount = amount;
	}

	public static class HealBuilder {

		private int targetX, targetY;
		private long id;
		private long timestamp;
		private int actor_id;
		private int amount;

		public HealBuilder (long id) {
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

		public int getAmount () {
			return amount;
		}

		public void setAmount (int amount) {
			this.amount = amount;
		}

		public Heal createEvent () {
			return new Heal (id, timestamp, actor_id, targetX, targetY, amount);
		}
	}
}

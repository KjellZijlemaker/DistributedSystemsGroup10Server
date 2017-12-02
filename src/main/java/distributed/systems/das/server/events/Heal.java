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
	private Heal (long id, long timestamp, String actor_id,
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
		private String actor_id;
		private int amount;

		public HealBuilder (long id) {
			this.id = id;
		}

		public int getTargetX () {
			return targetX;
		}

		public HealBuilder setTargetX (int targetX) {
			this.targetX = targetX;
			return this;
		}

		public int getTargetY () {
			return targetY;
		}

		public HealBuilder setTargetY (int targetY) {
			this.targetY = targetY;
			return this;
		}

		public long getId () {
			return id;
		}

		public HealBuilder setId (long id) {
			this.id = id;
			return this;
		}

		public long getTimestamp () {
			return timestamp;
		}

		public HealBuilder setTimestamp (long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public String getActor_id () {
			return actor_id;
		}

		public HealBuilder setActor_id (String actor_id) {
			this.actor_id = actor_id;
			return this;
		}

		public int getAmount () {
			return amount;
		}

		public HealBuilder setAmount (int amount) {
			this.amount = amount;
			return this;
		}

		public Heal createEvent () {
			return new Heal (id, timestamp, actor_id, targetX, targetY, amount);
		}
	}
}

package distributed.systems.das.server.events;

public class Attack extends Event {

	private int targetX, targetY;
	private int damage;

	private Attack (long id, long timestamp, int actor_id,
					int targetX, int targetY, int damage) {
		super (id, timestamp, actor_id);
		this.targetX = targetX;
		this.targetY = targetY;
		this.damage = damage;
	}

	@Override
	public int getType () {
		return ATTACK;
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

	public int getDamage () {
		return damage;
	}

	public void setDamage (int damage) {
		this.damage = damage;
	}

	public static class AttackBuilder {

		private int targetX, targetY;
		private long id;
		private long timestamp;
		private int actor_id;
		private int damage;

		public AttackBuilder (long id) {
			this.id = id;
		}

		public int getTargetX () {
			return targetX;
		}

		public AttackBuilder setTargetX (int targetX) {
			this.targetX = targetX;
			return this;
		}

		public int getTargetY () {
			return targetY;
		}

		public AttackBuilder setTargetY (int targetY) {
			this.targetY = targetY;
			return this;
		}

		public long getId () {
			return id;
		}

		public AttackBuilder setId (long id) {
			this.id = id;
			return this;
		}

		public long getTimestamp () {
			return timestamp;
		}

		public AttackBuilder setTimestamp (long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public int getActor_id () {
			return actor_id;
		}

		public AttackBuilder setActor_id (int actor_id) {
			this.actor_id = actor_id;
			return this;
		}

		public int getDamage () {
			return damage;
		}

		public AttackBuilder setDamage (int damage) {
			this.damage = damage;
			return this;
		}

		public Attack createEvent () {
			return new Attack (id, timestamp, actor_id, targetX, targetY, damage);
		}
	}

}

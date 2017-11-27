package distributed.systems.das.events;

public class Attack extends Event {

	private int targetX, targetY;
	private int damage;

	public Attack (long id, long timestamp, int actor_id,
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

		public int getDamage () {
			return damage;
		}

		public void setDamage (int damage) {
			this.damage = damage;
		}

		public Attack createEvent () {
			return new Attack (id, timestamp, actor_id, targetX, targetY, damage);
		}
	}

}

package distributed.systems.das.server.bot;

public class Bot {

	private int id;
	private double timestamp;
	private double lifespan;

	public Bot(int id, double timestamp, double lifespan) {
		// super();
		this.id = id;
		this.timestamp = timestamp;
		this.lifespan = lifespan;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	public double getLifespan() {
		return lifespan;
	}

	public void setLifespan(double lifespan) {
		this.lifespan = lifespan;
	}
	
	public int compareTimestamp(Bot that) {
		if (that.getTimestamp() < this.timestamp) {
			return 1;
		} else if (that.getTimestamp() > this.getTimestamp()) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "Bot [id=" + id + ", timestamp=" + timestamp
				+ ", lifespan=" + lifespan + "]";
	}

}

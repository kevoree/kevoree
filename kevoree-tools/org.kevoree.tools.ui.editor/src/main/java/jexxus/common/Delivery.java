package jexxus.common;

/**
 * Used for the requirements of message delivery.
 * 
 * @author Jason
 * 
 */
public class Delivery {

	/**
	 * These messages will always reach the destination unless there is a
	 * network failure.
	 */
	public static final Delivery RELIABLE = new Delivery("TCP");

	/**
	 * These messages are not guaranteed to reach the destination, but they have
	 * the advantage of being faster.
	 */
	public static final Delivery UNRELIABLE = new Delivery("UDP");

	private final String type;

	private Delivery(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}

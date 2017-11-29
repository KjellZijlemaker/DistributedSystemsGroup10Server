package distributed.systems.das.server.util;

public class AlreadyRunningException extends Exception {

	public AlreadyRunningException (Class c) {
		super (c.getName () + " is already running!");
	}

}

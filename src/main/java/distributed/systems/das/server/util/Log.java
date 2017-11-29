package distributed.systems.das.server.util;

import java.util.logging.Logger;

public class Log {

	public static void throwException (Exception e, Class c) {
		Logger.getLogger (c.toString ()).severe (e.getMessage ());
	}

}

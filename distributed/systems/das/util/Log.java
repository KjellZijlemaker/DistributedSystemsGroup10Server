package distributed.systems.das.util;

import java.util.logging.Logger;

/**
 * Created by Jasper van Riet
 */
public class Log {

	public static void throwException (Exception e, Class c) {
		Logger.getLogger (c.toString ()).severe (e.getMessage ());
	}

}

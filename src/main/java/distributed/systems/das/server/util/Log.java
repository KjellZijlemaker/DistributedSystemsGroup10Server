package distributed.systems.das.server.util;

import distributed.systems.das.server.Core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	public final String SERVERID = "Node-1";
	private static Logger logger = Logger.getLogger("myLog");
	private static FileHandler fh;

	public Log(){
		// Create log file
		SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
		try {
			fh = new FileHandler("src/main/resources/Log/" + SERVERID + "-"
					+ format.format(Calendar.getInstance().getTime()) + ".log");
		} catch (Exception e) {
			e.printStackTrace();
		}
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
		logger.addHandler(fh);
	}

	public static void throwException (Exception e, Class c) {
		logger.severe(e.getMessage());
//		logger.getLogger (c.toString ()).severe (e.getMessage ());
	}

	public static void serverUpdate(String update){
		logger.info(update);
		//logger.getLogger(c.toString()).info(update);
	}

	public static void serverError(String error){
		logger.severe(error);
//		logger.getLogger(c.toString()).severe(error);
	}


}

package org.openkinect.freenect.util;

import org.openkinect.freenect.Device;
import org.openkinect.freenect.LogHandler;
import org.openkinect.freenect.LogLevel;
import org.slf4j.LoggerFactory;

public class Jdk14LogHandler implements LogHandler {

    //private final Logger logger = Logger.getLogger("freenect");
	private org.slf4j.Logger logger = LoggerFactory.getLogger("freenect");
    //private final EnumMap<LogLevel, Level> levelMap = new EnumMap<LogLevel, Level>(LogLevel.class);
  
    public Jdk14LogHandler() {
        /*logger.setLevel(Level.ALL);
        levelMap.put(LogLevel.FATAL, Level.SEVERE);
        levelMap.put(LogLevel.ERROR, Level.SEVERE);
        levelMap.put(LogLevel.WARNING, Level.WARNING);
        levelMap.put(LogLevel.NOTICE, Level.CONFIG);
        levelMap.put(LogLevel.INFO, Level.INFO);
        levelMap.put(LogLevel.DEBUG, Level.FINE);
        levelMap.put(LogLevel.SPEW, Level.FINER);
        levelMap.put(LogLevel.FLOOD, Level.FINEST);*/
    }
  
    @Override
    public void onMessage(Device dev, LogLevel level, String msg) {
		if (level.equals(LogLevel.DEBUG)) {
			logger.debug("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.ERROR)) {
			logger.error("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.FATAL)) {
			logger.error("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.FLOOD)) {
			logger.debug("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.INFO)) {
			logger.info("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.NOTICE)) {
			logger.info("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.SPEW)) {
			logger.error("device " + dev.getDeviceIndex() + ": " + msg);
		} else if (level.equals(LogLevel.WARNING)) {
			logger.debug("device " + dev.getDeviceIndex() + ": " + msg);
		}
        //logger.log(levelMap.get(level), "device " + dev.getDeviceIndex() + ": " + msg);
    }
}

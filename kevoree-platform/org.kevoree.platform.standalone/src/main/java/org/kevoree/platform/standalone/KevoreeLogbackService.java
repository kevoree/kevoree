package org.kevoree.platform.standalone;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/03/12
 * Time: 14:08
 */
public class KevoreeLogbackService implements KevoreeLogService {
	@Override
	public void setCoreLogLevel (KevoreeLogLevel kevoreeLogLevel) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		Level logLevel = convertKevoreeLogLeveltoLogbackLogLevel(kevoreeLogLevel);
		for (Logger logger : root.getLoggerContext().getLoggerList()) {
			if (logger.getName().startsWith("org.kevoree.framework")
					|| logger.getName().startsWith("org.kevoree.core")
					|| logger.getName().startsWith("org.kevoree.baseChecker")
					|| logger.getName().startsWith("org.kevoree.kcl")
					|| logger.getName().startsWith("org.kevoree.merger")
					|| logger.getName().startsWith("org.kevoree.model")
					|| logger.getName().startsWith("org.kevoree.api")) {
				logger.setLevel(logLevel);
			}
		}
	}

	@Override
	public void setUserLogLevel (KevoreeLogLevel kevoreeLogLevel) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		Level logLevel = convertKevoreeLogLeveltoLogbackLogLevel(kevoreeLogLevel);
		root.setLevel(logLevel);
	}

	@Override
	public void setLogLevel (String s, KevoreeLogLevel kevoreeLogLevel) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		for (Logger logger : root.getLoggerContext().getLoggerList()) {
			if (logger.getName().equals(s)) {
				logger.setLevel(convertKevoreeLogLeveltoLogbackLogLevel(kevoreeLogLevel));
			}
		}
	}

	@Override
	public KevoreeLogLevel getCoreLogLevel () {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		for (Logger logger : root.getLoggerContext().getLoggerList()) {
			if (logger.getName().startsWith("org.kevoree.framework")
					|| logger.getName().startsWith("org.kevoree.core")
					|| logger.getName().startsWith("org.kevoree.baseChecker")
					|| logger.getName().startsWith("org.kevoree.kcl")
					|| logger.getName().startsWith("org.kevoree.merger")
					|| logger.getName().startsWith("org.kevoree.model")
					|| logger.getName().startsWith("org.kevoree.api")) {
				return convertLogbackLogLeveltoKevoreeLogLevel(logger.getLevel());
			}
		}
		return null;
	}

	@Override
	public KevoreeLogLevel getUserLogLevel () {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		return convertLogbackLogLeveltoKevoreeLogLevel(root.getLevel());
	}

	@Override
	public KevoreeLogLevel getLogLevel (String s) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
				for (Logger logger : root.getLoggerContext().getLoggerList()) {
					if (logger.getName().equals(s)) {
						return convertLogbackLogLeveltoKevoreeLogLevel(logger.getLevel());
					}
				}
		return null;
	}

	private Level convertKevoreeLogLeveltoLogbackLogLevel (KevoreeLogLevel kevoreeLogLevel) {
		if (kevoreeLogLevel == KevoreeLogLevel.DEBUG) {
			return Level.DEBUG;
		}
		if (kevoreeLogLevel == KevoreeLogLevel.WARN) {
			return Level.WARN;
		}
		if (kevoreeLogLevel == KevoreeLogLevel.INFO) {
			return Level.INFO;
		}
		if (kevoreeLogLevel == KevoreeLogLevel.ERROR) {
			return Level.ERROR;
		}
		if (kevoreeLogLevel == KevoreeLogLevel.FINE) {
			return Level.ALL;
		}
		return Level.INFO;
	}

	private KevoreeLogLevel convertLogbackLogLeveltoKevoreeLogLevel (Level kevoreeLogLevel) {
		if (kevoreeLogLevel == Level.DEBUG) {
			return KevoreeLogLevel.DEBUG;
		}
		if (kevoreeLogLevel == Level.WARN) {
			return KevoreeLogLevel.WARN;
		}
		if (kevoreeLogLevel == Level.INFO) {
			return KevoreeLogLevel.INFO;
		}
		if (kevoreeLogLevel == Level.ERROR) {
			return KevoreeLogLevel.ERROR;
		}
		if (kevoreeLogLevel == Level.ALL) {
			return KevoreeLogLevel.FINE;
		}
		return KevoreeLogLevel.INFO;
	}
}

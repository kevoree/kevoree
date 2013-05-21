/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Log {
    /**
     * No logging at all.
     */
    static public final int LEVEL_NONE = 6;
    /**
     * Critical errors. The application may no longer work correctly.
     */
    static public final int LEVEL_ERROR = 5;
    /**
     * Important warnings. The application will continue to work correctly.
     */
    static public final int LEVEL_WARN = 4;
    /**
     * Informative messages. Typically used for deployment.
     */
    static public final int LEVEL_INFO = 3;
    /**
     * Debug messages. This level is useful during development.
     */
    static public final int LEVEL_DEBUG = 2;
    /**
     * Trace messages. A lot of information is logged, so this level is usually only needed when debugging a problem.
     */
    static public final int LEVEL_TRACE = 1;

    /**
     * The level of messages that will be logged. Compiling this and the booleans below as "final" will cause the compiler to
     * remove all "if (Log.info) ..." type statements below the set level.
     */
    static private int level = LEVEL_INFO;

    /**
     * True when the ERROR level will be logged.
     */
    static public boolean ERROR = level <= LEVEL_ERROR;
    /**
     * True when the WARN level will be logged.
     */
    static public boolean WARN = level <= LEVEL_WARN;
    /**
     * True when the INFO level will be logged.
     */
    static public boolean INFO = level <= LEVEL_INFO;
    /**
     * True when the DEBUG level will be logged.
     */
    static public boolean DEBUG = level <= LEVEL_DEBUG;
    /**
     * True when the TRACE level will be logged.
     */
    static public boolean TRACE = level <= LEVEL_TRACE;

    /**
     * Sets the level to log. If a version of this class is being used that has a final log level, this has no affect.
     */
    static public void set(int level) {
        // Comment out method contents when compiling fixed level JARs.
        Log.level = level;
        ERROR = level <= LEVEL_ERROR;
        WARN = level <= LEVEL_WARN;
        INFO = level <= LEVEL_INFO;
        DEBUG = level <= LEVEL_DEBUG;
        TRACE = level <= LEVEL_TRACE;
    }

    static public void NONE() {
        set(LEVEL_NONE);
    }

    static public void ERROR() {
        set(LEVEL_ERROR);
    }

    static public void WARN() {
        set(LEVEL_WARN);
    }

    static public void INFO() {
        set(LEVEL_INFO);
    }

    static public void DEBUG() {
        set(LEVEL_DEBUG);
    }

    static public void TRACE() {
        set(LEVEL_TRACE);
    }

    /**
     * Sets the logger that will write the log messages.
     */
    static public void setLogger(Logger logger) {
        Log.logger = logger;
    }

    private Log() {

    }

    private Log(Logger plogger) {
        logger = plogger;
    }

    static public Log getLog(String category) {
        Logger newLogger = new Logger();
        newLogger.setCategory(category);
        return new Log(newLogger);
    }

    static private Logger logger = new Logger();

    private static String processMessage(String message, String... params) {
        String optimizeMessage = message;
        //TODO optimize
        for (Object o : params) {
            optimizeMessage = optimizeMessage.replaceFirst("\\{\\}", o.toString());
        }
        return optimizeMessage;
    }

    static public void error(String message, String... params) {
        if (ERROR) {
            error(processMessage(message, params));
        }
    }

    static public void error(String message,Throwable ex, String... params) {
        if (ERROR) {
            error(processMessage(message, params),ex);
        }
    }

    static public void error(String message, Throwable ex) {
        if (ERROR) logger.log(LEVEL_ERROR, message, ex);
    }

    static public void error(String message) {
        if (ERROR) logger.log(LEVEL_ERROR, message, null);
    }

    static public void warn(String message, Throwable ex) {
        if (WARN) logger.log(LEVEL_WARN, message, ex);
    }

    static public void warn(String message, String... params) {
        if (WARN) {
            warn(processMessage(message, params));
        }
    }

    static public void warn(String message, Throwable ex, String... params) {
        if (WARN) {
            warn(processMessage(message, params),ex);
        }
    }

    static public void warn(String message) {
        if (WARN) logger.log(LEVEL_WARN, message, null);
    }

    static public void info(String message, Throwable ex) {
        if (INFO) logger.log(LEVEL_INFO, message, ex);
    }

    static public void info(String message, String... params) {
        if (INFO) {
            info(processMessage(message, params));
        }
    }

    static public void info(String message, Throwable ex, String... params) {
        if (INFO) {
            info(processMessage(message, params),ex);
        }
    }

    static public void info(String message) {
        if (INFO) logger.log(LEVEL_INFO, message, null);
    }

    static public void debug(String message, Throwable ex) {
        if (DEBUG) logger.log(LEVEL_DEBUG, message, ex);
    }

    static public void debug(String message) {
        if (DEBUG) logger.log(LEVEL_DEBUG, message, null);
    }

    static public void debug(String message, String... params) {
        if (DEBUG) {
            debug(processMessage(message, params));
        }
    }

    static public void debug(String message, Throwable ex, String... params) {
        if (DEBUG) {
            debug(processMessage(message, params),ex);
        }
    }

    static public void trace(String message, Throwable ex) {
        if (TRACE) logger.log(LEVEL_TRACE, message, ex);
    }

    static public void trace(String message) {
        if (TRACE) logger.log(LEVEL_TRACE, message, null);
    }

    static public void trace(String message, String... params) {
        if (TRACE) {
            trace(processMessage(message, params));
        }
    }

    static public void trace(String message, Throwable ex, String... params) {
        if (TRACE) {
            trace(processMessage(message, params),ex);
        }
    }

    /**
     * Performs the actual logging. Default implementation logs to System.out. Extended and use {@link Log#logger} set to handle
     * logging differently.
     */
    static public class Logger {
        private long firstLogTime = new Date().getTime();
        private static final String error_msg = " ERROR: ";
        private static final String warn_msg = " WARN: ";
        private static final String info_msg = " INFO: ";
        private static final String debug_msg = " DEBUG: ";
        private static final String trace_msg = " TRACE: ";

        private String category = null;

        public void setCategory(String category) {
            this.category = category;
        }

        public void log(int level, String message, Throwable ex) {
            StringBuilder builder = new StringBuilder(256);
            long time = new Date().getTime() - firstLogTime;
            long minutes = time / (1000 * 60);
            long seconds = time / (1000) % 60;
            if (minutes <= 9) builder.append('0');
            builder.append(minutes);
            builder.append(':');
            if (seconds <= 9) builder.append('0');
            builder.append(seconds);
            switch (level) {
                case LEVEL_ERROR:
                    builder.append(error_msg);
                    break;
                case LEVEL_WARN:
                    builder.append(warn_msg);
                    break;
                case LEVEL_INFO:
                    builder.append(info_msg);
                    break;
                case LEVEL_DEBUG:
                    builder.append(debug_msg);
                    break;
                case LEVEL_TRACE:
                    builder.append(trace_msg);
                    break;
            }
            if (category != null) {
                builder.append('[');
                builder.append(category);
                builder.append("] ");
            }
            builder.append(message);
            if (ex != null) {
                StringWriter writer = new StringWriter(256);
                ex.printStackTrace(new PrintWriter(writer));
                builder.append('\n');
                builder.append(writer.toString().trim());
            }
            print(builder.toString());
        }

        /**
         * Prints the message to System.out. Called by the default implementation of {@link #log(int, String, Throwable)}.
         */
        protected void print(String message) {
            System.out.println(message);
        }
    }
}
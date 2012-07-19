/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.kevoree.platform.standalone.min;
//
//import org.kevoree.api.service.core.logging.KevoreeLogLevel;
//import org.kevoree.api.service.core.logging.KevoreeLogService;
//import org.slf4j.LoggerFactory;
//import org.slf4j.impl.SimpleLogger;
//import org.slf4j.impl.SimpleLoggerFactory;
//
///**
// * Created with IntelliJ IDEA.
// * User: duke
// * Date: 13/07/12
// * Time: 01:41
// */
//public class SimpleLogService implements KevoreeLogService {
//
//    KevoreeLogLevel baseLevel = KevoreeLogLevel.WARN;
//
//    KevoreeLogLevel userLevel = KevoreeLogLevel.INFO;
//
//    @Override
//    public void setCoreLogLevel(KevoreeLogLevel kevoreeLogLevel) {
//
//        System.out.println("DF="+SimpleLoggerFactory.INSTANCE.getAllLoggerName().size());
//
//        for (String loggerName : SimpleLoggerFactory.INSTANCE.getAllLoggerName()) {
//            SimpleLogger logger = (SimpleLogger) SimpleLoggerFactory.INSTANCE.getLogger(loggerName);
//            if (loggerName.startsWith("org.kevoree.framework")
//                    || loggerName.startsWith("org.kevoree.core")
//                    || loggerName.startsWith("org.kevoree.baseChecker")
//                    || loggerName.startsWith("org.kevoree.kcl")
//                    || loggerName.startsWith("org.kevoree.merger")
//                    || loggerName.startsWith("org.kevoree.model")
//                    || loggerName.startsWith("org.kevoree.api")
//                    || loggerName.startsWith("org.kevoree.tools.aether")) {
//                logger.currentLogLevel = getSimpleLevelFromKevoreeLevel(kevoreeLogLevel);
//            }
//        }
//    }
//
//    @Override
//    public void setUserLogLevel(KevoreeLogLevel kevoreeLogLevel) {
//        for (String loggerName : SimpleLoggerFactory.INSTANCE.getAllLoggerName()) {
//            SimpleLogger logger = (SimpleLogger) SimpleLoggerFactory.INSTANCE.getLogger(loggerName);
//            if (loggerName.startsWith("org.kevoree.framework")
//                    || loggerName.startsWith("org.kevoree.core")
//                    || loggerName.startsWith("org.kevoree.baseChecker")
//                    || loggerName.startsWith("org.kevoree.kcl")
//                    || loggerName.startsWith("org.kevoree.merger")
//                    || loggerName.startsWith("org.kevoree.model")
//                    || loggerName.startsWith("org.kevoree.api")
//                    || loggerName.startsWith("org.kevoree.tools.aether")) {
//            } else {
//                logger.currentLogLevel = getSimpleLevelFromKevoreeLevel(kevoreeLogLevel);
//            }
//        }
//    }
//
//    @Override
//    public void setLogLevel(String s, KevoreeLogLevel kevoreeLogLevel) {
//        SimpleLogger logger = (SimpleLogger) SimpleLoggerFactory.INSTANCE.getLogger(s);
//        logger.currentLogLevel = getSimpleLevelFromKevoreeLevel(kevoreeLogLevel);
//    }
//
//    @Override
//    public KevoreeLogLevel getCoreLogLevel() {
//        return baseLevel;
//    }
//
//    @Override
//    public KevoreeLogLevel getUserLogLevel() {
//        return userLevel;
//    }
//
//    @Override
//    public KevoreeLogLevel getLogLevel(String s) {
//        SimpleLogger logger = (SimpleLogger) SimpleLoggerFactory.INSTANCE.getLogger(s);
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_ALL){
//            return KevoreeLogLevel.FINE;
//        }
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_DEBUG){
//            return KevoreeLogLevel.DEBUG;
//        }
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_ERROR){
//            return KevoreeLogLevel.ERROR;
//        }
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_INFO){
//            return KevoreeLogLevel.INFO;
//        }
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_TRACE){
//            return KevoreeLogLevel.FINE;
//        }
//        if(logger.currentLogLevel == SimpleLogger.LOG_LEVEL_WARN){
//            return KevoreeLogLevel.WARN;
//        }
//        return userLevel;
//    }
//
//    public int getSimpleLevelFromKevoreeLevel(KevoreeLogLevel level) {
//        if (level.equals(KevoreeLogLevel.INFO)) {
//            return SimpleLogger.LOG_LEVEL_INFO;
//        }
//        if (level.equals(KevoreeLogLevel.WARN)) {
//            return SimpleLogger.LOG_LEVEL_WARN;
//        }
//        if (level.equals(KevoreeLogLevel.DEBUG)) {
//            return SimpleLogger.LOG_LEVEL_DEBUG;
//        }
//        if (level.equals(KevoreeLogLevel.ERROR)) {
//            return SimpleLogger.LOG_LEVEL_ERROR;
//        }
//        if (level.equals(KevoreeLogLevel.FINE)) {
//            return SimpleLogger.LOG_LEVEL_ALL;
//        }
//        return SimpleLogger.LOG_LEVEL_INFO;
//    }
//
//
//}

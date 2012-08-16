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
package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

/**
 * An implementation of {@link org.slf4j.ILoggerFactory} which always returns
 * {@link org.slf4j.impl.SimpleLogger} instances.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class SimpleLoggerFactory implements ILoggerFactory {

    public final static SimpleLoggerFactory INSTANCE = new SimpleLoggerFactory();

    private final static Map<String, SimpleLogger> loggerMap = new HashMap<String, SimpleLogger>();

    public Set<String> getAllLoggerName() {
        return loggerMap.keySet();
    }

    public SimpleLoggerFactory() {

    }

    /**
     * Return an appropriate {@link org.slf4j.impl.SimpleLogger} instance by name.
     */
    public Logger getLogger(String name) {
        SimpleLogger slogger = null;
        // protect against concurrent access of the loggerMap
        synchronized (this) {
            slogger = (SimpleLogger) loggerMap.get(name);
            if (slogger == null) {
                slogger = new SimpleLogger(name);
                loggerMap.put(name, slogger);
            }
        }
        return slogger;
    }
}

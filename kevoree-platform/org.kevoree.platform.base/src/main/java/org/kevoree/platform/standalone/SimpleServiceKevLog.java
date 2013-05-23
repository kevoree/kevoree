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
package org.kevoree.platform.standalone;

import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;
import org.kevoree.log.Log;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/13
 * Time: 10:53
 */
public class SimpleServiceKevLog implements KevoreeLogService {
    @Override
    public void setCoreLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        //setLogLevel(null, kevoreeLogLevel);
    }

    @Override
    public void setUserLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        setLogLevel(null, kevoreeLogLevel);
    }

    @Override
    public void setLogLevel(String s, KevoreeLogLevel kevoreeLogLevel) {
        if (kevoreeLogLevel.equals(KevoreeLogLevel.FINE)) {
            Log.set(Log.LEVEL_TRACE);
            return;
        }
        if (kevoreeLogLevel.equals(KevoreeLogLevel.DEBUG)) {
            Log.set(Log.LEVEL_DEBUG);
            return;
        }
        if (kevoreeLogLevel.equals(KevoreeLogLevel.INFO)) {
            Log.set(Log.LEVEL_INFO);
            return;
        }
        if (kevoreeLogLevel.equals(KevoreeLogLevel.WARN)) {
            Log.set(Log.LEVEL_WARN);
            return;
        }
        if (kevoreeLogLevel.equals(KevoreeLogLevel.ERROR)) {
            Log.set(Log.LEVEL_ERROR);
            return;
        }
    }

    @Override
    public KevoreeLogLevel getCoreLogLevel() {
        return getUserLogLevel();
    }

    @Override
    public KevoreeLogLevel getUserLogLevel() {
        return getUserLogLevel();
    }

    @Override
    public KevoreeLogLevel getLogLevel(String s) {
        if (Log.TRACE) {
            return KevoreeLogLevel.FINE;
        }
        if (Log.DEBUG) {
            return KevoreeLogLevel.DEBUG;
        }
        if (Log.INFO) {
            return KevoreeLogLevel.INFO;
        }
        if (Log.WARN) {
            return KevoreeLogLevel.WARN;
        }
        if (Log.ERROR) {
            return KevoreeLogLevel.ERROR;
        }
        return KevoreeLogLevel.INFO;
    }
}

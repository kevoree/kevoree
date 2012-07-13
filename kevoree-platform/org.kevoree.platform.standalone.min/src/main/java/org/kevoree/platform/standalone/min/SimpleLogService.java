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
package org.kevoree.platform.standalone.min;

import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 13/07/12
 * Time: 01:41
 */
public class SimpleLogService implements KevoreeLogService {

    KevoreeLogLevel baseLevel = KevoreeLogLevel.WARN;

    KevoreeLogLevel userLevel = KevoreeLogLevel.INFO;

    @Override
    public void setCoreLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setUserLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLogLevel(String s, KevoreeLogLevel kevoreeLogLevel) {
    }

    @Override
    public KevoreeLogLevel getCoreLogLevel() {
        return baseLevel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getUserLogLevel() {
        return userLevel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getLogLevel(String s) {
        return userLevel;
    }
}

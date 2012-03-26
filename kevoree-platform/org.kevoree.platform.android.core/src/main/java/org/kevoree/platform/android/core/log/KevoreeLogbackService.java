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
package org.kevoree.platform.android.core.log;

import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/03/12
 * Time: 14:08
 */
public class KevoreeLogbackService implements KevoreeLogService {
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getCoreLogLevel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getUserLogLevel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getLogLevel(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

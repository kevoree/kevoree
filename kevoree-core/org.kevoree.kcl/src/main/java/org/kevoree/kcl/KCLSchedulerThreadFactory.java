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
package org.kevoree.kcl;

import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/24/13
 * Time: 8:58 AM
 */
public class KCLSchedulerThreadFactory implements ThreadFactory {

    private ThreadGroup group = null;

    public KCLSchedulerThreadFactory(){
        SecurityManager s = System.getSecurityManager();

        if (s != null) {
            group= s.getThreadGroup();
        } else {
            group = Thread.currentThread().getThreadGroup();
        }
    }


    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(group, runnable, "KCL_Scheduler");
        if (!t.isDaemon()) {
            t.setDaemon(true);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}

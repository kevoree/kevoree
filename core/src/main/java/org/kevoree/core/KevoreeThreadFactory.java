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
package org.kevoree.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/28/13
 * Time: 3:13 PM                       ;
 */
public class KevoreeThreadFactory implements ThreadFactory {

    private ThreadGroup group;
    private String name;

    public KevoreeThreadFactory(String name) {
        this.name = name;
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            group = s.getThreadGroup();
        } else {
            group = Thread.currentThread().getThreadGroup();
        }
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        Thread t = new Thread(group, runnable, "kev_" + name);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}

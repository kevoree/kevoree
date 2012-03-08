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
package org.kevoree.platform.android.boot.controller;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 11:43
 */
abstract class AController {

    private static final String TAG = AController.class.getSimpleName();

    private final List<Handler> outboxHandlers = new ArrayList<Handler>();

    abstract public boolean handleMessage(Request req, Object data);
    abstract public boolean handleMessage(Request req,String key, View data);

    public boolean handleMessage(Request req) {
        return handleMessage(req, null);
    }

    public final void addOutboxHandler(Handler handler) {
        outboxHandlers.add(handler);
    }

    public final void removeOutboxHandler(Handler handler) {
        outboxHandlers.remove(handler);
    }

    protected final void notifyOutboxHandlers(int req, int arg1, int arg2, Object obj) {
        if (!outboxHandlers.isEmpty()) {
            for (Handler handler : outboxHandlers) {
                Message msg = Message.obtain(handler, req, arg1, arg2, obj);
                msg.sendToTarget();
            }
        }
    }
}

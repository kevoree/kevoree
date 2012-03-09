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

import android.util.Log;
import android.view.View;
import org.kevoree.platform.android.boot.view.ManagerUI;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 11:46
 */
public class KController extends AController  {

    private static final String TAG = KController.class.getSimpleName();

	private ManagerUI viewmanager;
	private IController controller;

	public ManagerUI getViewManager(){
		return viewmanager;
	}

    public KController(ManagerUI _viewmanager) {
    	this.viewmanager = _viewmanager;
        controller = new ControllerImpl(this);
	}

	@Override
	public boolean handleMessage(Request req) {
		Log.i(TAG, "handling message code of " + req);
		return controller.handleMessage(req);
	}

	@Override
	public boolean handleMessage(Request req, Object data) {
		Log.i(TAG, "handling message code of " + req);
		return controller.handleMessage(req, data);
	}

	@Override
	public boolean handleMessage(Request req, String key, View data) {
		Log.i(TAG, "handling message code of " + req);
		return controller.handleMessage(req,key, data);
	}


}
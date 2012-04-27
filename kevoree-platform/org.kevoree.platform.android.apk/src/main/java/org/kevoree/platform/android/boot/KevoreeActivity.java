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
package org.kevoree.platform.android.boot;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import org.kevoree.platform.android.boot.controller.ControllerImpl;
import org.kevoree.platform.android.boot.controller.IController;
import org.kevoree.platform.android.boot.controller.Request;
import org.kevoree.platform.android.boot.utils.OnChangeListener;
import org.kevoree.platform.android.boot.view.BaseKevoreeUI;
import org.kevoree.platform.android.boot.view.ManagerUI;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 12:05
 */
public class KevoreeActivity extends FragmentActivity implements  OnChangeListener<ManagerUI> {

    private static final String TAG = KevoreeActivity.class.getSimpleName();

   public static IController controller=null;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState)
    {

        System.setProperty("actors.enableForkJoin", "false");
        System.setProperty("actors.maxPoolSize", "256");

        super.onCreate(savedInstanceState);

        if(controller == null)
        {
            controller = new ControllerImpl(this);
            BaseKevoreeUI uiBase =  new BaseKevoreeUI(this,controller);
            // add Kevoree UI BASE
            controller.handleMessage(Request.ADD_TO_GROUP,"KAdmin",uiBase);
        }
        else
        {
            controller.getViewManager().restoreViews(this);
        }
    }


    @Override
    public void onChange(ManagerUI model) {
        /*
        Log.i(TAG,"onChange triggered") ;
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                 // a changement has been dectected
            }
        });
        ;
         */
    }
}

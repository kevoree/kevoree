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
package org.kevoree.platform.android.core

import org.kevoree.android.framework.service.KevoreeAndroidService
import android.app.Activity
import android.view.View
import android.widget.TabHost

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 17:52
 */

class KevoreeActivityAndroidService(act : Activity,tabs:TabHost) extends KevoreeAndroidService {

  def getRootActivity = act

  def addToGroup(groupKey: String, view: View) {

    System.out.println("HelloAddGroup");

    act.runOnUiThread(new Runnable() {
      @Override
      def run() {

        System.out.println("InRun "+act+"-"+tabs);

        val tspec3 = tabs.newTabSpec(groupKey);
        tspec3.setIndicator(groupKey);//, getResources().getDrawable(android.R.drawable.star_on));
        tspec3.setContent(new PreExistingViewFactory(view));
        tabs.addTab(tspec3);
      }
    });
  }

  def remove(p1: View) {}
}

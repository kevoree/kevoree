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
package org.kevoree.platform.android.core

import org.kevoree.api.service.core.handler.ModelListener
import org.kevoree.ContainerRoot
import android.app.{Activity, ProgressDialog}
import android.widget.Toast

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/10/12
 * Time: 13:59
 */
class ProgressDialogModelListener(ctx : Activity) extends ModelListener {

  private var dialog : ProgressDialog = null

  def preUpdate(currentModel: ContainerRoot, proposedModel: ContainerRoot) = {
    ctx.runOnUiThread(new Runnable {
      def run() {
        dialog = ProgressDialog.show(ctx, "Kevoree","Performing adaptation, please wait...");
        dialog.setProgress(0)
      }
    })

    true
  }

  def initUpdate(currentModel: ContainerRoot, proposedModel: ContainerRoot) = {
    ctx.runOnUiThread(new Runnable {
      def run() {
        dialog.setProgress(10)
      }
    })
    true
  }

  def afterLocalUpdate(currentModel: ContainerRoot, proposedModel: ContainerRoot) = {
    ctx.runOnUiThread(new Runnable {
      def run() {
        dialog.setProgress(100)
        dialog.dismiss()
      }
    })
    true
  }

  def modelUpdated() {
    ctx.runOnUiThread(new Runnable {
      def run() {
        Toast.makeText(ctx, "Platform updated!", Toast.LENGTH_SHORT);
      }
    })
  }

}

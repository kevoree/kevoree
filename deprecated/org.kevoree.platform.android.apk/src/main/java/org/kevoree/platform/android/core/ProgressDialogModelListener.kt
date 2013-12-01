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
import android.app.Activity
import android.app.ProgressDialog
import android.widget.Toast
import android.util.Log

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/10/12
 * Time: 13:59
 */
class ProgressDialogModelListener(val ctx : Activity) : ModelListener {

  private var dialog : ProgressDialog? = null

  override fun preUpdate(currentModel: ContainerRoot?, proposedModel: ContainerRoot?) : Boolean {
    ctx.runOnUiThread(object : Runnable {
      override fun run() {
        dialog = ProgressDialog.show(ctx, "Kevoree","Performing adaptation, please wait...",true,true);
        dialog!!.setProgress(0)
      }
    })
    return true
  }

    override fun initUpdate(currentModel: ContainerRoot?, proposedModel: ContainerRoot?) :Boolean {
    ctx.runOnUiThread(object : Runnable {
      override fun run() {
        dialog!!.setProgress(10)
      }
    })
    return true
  }

  override fun afterLocalUpdate(currentModel: ContainerRoot?, proposedModel: ContainerRoot?):Boolean {
   ctx.runOnUiThread(object : Runnable {
      override fun run() {
        dialog!!.setProgress(100)
        dialog!!.dismiss()
      }
    })
    return true
  }

  override fun modelUpdated() {
    ctx.runOnUiThread(object : Runnable {
      override fun run() {
        Toast.makeText(ctx, "Platform updated!", Toast.LENGTH_SHORT);
      }
    })
  }

  override fun postRollback(currentModel: ContainerRoot?, proposedModel: ContainerRoot?) {
    ctx.runOnUiThread(object : Runnable {
      override fun run() {
        dialog!!.dismiss()
        Toast.makeText(ctx, "Error detected, rollback performed!", Toast.LENGTH_SHORT);
      }
    })
    Log.e("Kevoree GUI","PostRollBack")
  }

  override fun preRollback(currentModel: ContainerRoot?, proposedModel: ContainerRoot?) {
    Log.e("Kevoree GUI","PreRollBack")

  }
}

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
package org.kevoree.kompare

import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 09:18
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  println("Test Kompare")

  val bean = new KevoreeKompareBean

  val model1 = KevoreeXmiHelper.load("/Users/duke/Desktop/step1.kev")
  val model2 = KevoreeXmiHelper.load("/Users/duke/Desktop/step2.kev")


  val adapModel = bean.kompare(model1,model2,"duke")

  adapModel.getAdaptations.foreach{ adaptation =>
    println(adaptation.getPrimitiveType.getName)

  }


}
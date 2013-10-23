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
package org.kevoree.core.basechecker

import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/10/12
 * Time: 15:04
 */
fun main(args: Array<String>) {
    val check = RootChecker()
    val res =check.check(KevoreeXmiHelper.load("/home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_1node_1componentKO.kev"))
//
//    /home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_1node_1componentOK.kev
//    /home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_single_nodeKO.kev
//    /home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_single_nodeOK.kev
//    /home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_two_nodeKO.kev
//    /home/edaubert/workspace/kevoree/kevoree-core/org.kevoree.basechecker/src/test/resources/test_checker/name/model_two_nodeOK.kev

    for (e in res) {
        println(e.getMessage())
    }
}

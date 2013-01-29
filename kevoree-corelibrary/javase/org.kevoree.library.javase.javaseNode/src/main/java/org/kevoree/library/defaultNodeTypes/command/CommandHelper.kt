package org.kevoree.library.defaultNodeTypes.command

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

import org.kevoree.DeployUnit

object CommandHelper {

    fun buildKEY(du: DeployUnit): String {
        return du.getName() + "/" + buildQuery(du, null)
    }

    fun buildQuery(du: DeployUnit, repoUrl: String?): String {
        val query = StringBuilder()
        query.append("mvn:")
        if(repoUrl != null){
            query.append(repoUrl); query.append("!")
        }
        query.append(du.getGroupName())
        query.append("/")
        query.append(du.getUnitName())
        if( !du.getVersion().equals("") && !du.getVersion().equals("default")){
            query.append("/"); query.append(du.getVersion())
        }
        return query.toString()
    }

}

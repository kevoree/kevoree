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
package org.kevoree.platform.agent;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/05/11
 * Time: 20:45
 */
public class App {

	public static void main(String[] args) {
		new KevoreeNodeRunner("duke", 8000).startNode();
		new KevoreeNodeRunner("duke1", 8001).startNode();
	}
}

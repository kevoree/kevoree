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
package org.kevoree.platform.osgi.standalone.gui;

import org.kevoree.platform.osgi.standalone.ConstantValuesImpl;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 11/10/11
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class GuiConstantValuesImpl extends ConstantValuesImpl implements GuiConstantValues{

    public String getIconUrl() { return "kevoree-logo-full.png";}
    public String getSmallIconUrl() { return "kev-logo-full.png";  }
    public String getBootstrapWindowTitle() { return "Kevoree runtime : node properties";}


}

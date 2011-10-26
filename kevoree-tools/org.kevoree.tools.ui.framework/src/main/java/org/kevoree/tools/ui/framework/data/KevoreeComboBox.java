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
package org.kevoree.tools.ui.framework.data;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 26/10/11
 * Time: 09:02
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class KevoreeComboBox extends JComboBox {

    private ListCellRenderer myRenderer = new NamedElementListRenderer();

    public KevoreeComboBox() {
        super();
        setRenderer(myRenderer);
    }

    public KevoreeComboBox(ComboBoxModel model) {
        super(model);
        setRenderer(myRenderer);
    }

    @Override
    public ListCellRenderer getRenderer() {
        System.out.println("Get Renderer");
        return myRenderer;
    }

}

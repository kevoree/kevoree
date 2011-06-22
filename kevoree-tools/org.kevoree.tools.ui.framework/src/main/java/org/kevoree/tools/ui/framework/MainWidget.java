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
package org.kevoree.tools.ui.framework;

import com.explodingpixels.macwidgets.*;
import org.kevoree.tools.ui.framework.elements.ModelPanel;
import org.kevoree.tools.ui.framework.listener.TypeDefinitionTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

/**
 * User: ffouquet
 * Date: 16/06/11
 * Time: 22:13
 */
public class MainWidget {


    public static void main(String[] args) {
        JFrame jframe = new JFrame("Art2 UI Tester");
        MacUtils.makeWindowLeopardStyle(jframe.getRootPane());
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setPreferredSize(new Dimension(800, 600));


        SourceListModel model = new SourceListModel();
        SourceListCategory category = new SourceListCategory("Category");
        model.addCategory(category);

        SourceListItem item = new SourceListItem("Item");


        model.addItemToCategory(item, category);
        SourceList sourceList = new SourceList(model);
        sourceList.setTransferHandler(new TypeDefinitionTransferHandler());
        sourceList.setColorScheme(new SourceListDarkColorScheme());

        sourceList.getComponent().setPreferredSize(new Dimension(200,200));


        ModelPanel modelpanel = new ModelPanel();
        modelpanel.setDropTarget(new DropTarget(){
            @Override
            public void drop(DropTargetDropEvent dropTargetDropEvent) {
                System.out.println("DROP");
                super.drop(dropTargetDropEvent);    //To change body of overridden methods use File | Settings | File Templates.
            }
        });

        jframe.add(modelpanel, BorderLayout.CENTER);
        jframe.add(sourceList.getComponent(), BorderLayout.WEST);


        jframe.pack();
        jframe.setVisible(true);

    }


}

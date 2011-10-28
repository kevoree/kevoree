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
/* $Id: Art2EditorPanel.java 13865 2010-12-14 17:13:37Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.panel;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListSelectionListener;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.kevoree.MBinding;
import org.kevoree.tools.ui.editor.KevoreeTypeEditorPanel;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.TypeDefinitionSourceList;
import org.kevoree.tools.ui.editor.property.BindingPropertyEditor;
import org.kevoree.tools.ui.editor.property.GroupBindingPropertyEditor;
import org.kevoree.tools.ui.editor.property.InstancePropertyEditor;
import org.kevoree.tools.ui.editor.property.NodePropertyEditor;
import org.kevoree.tools.ui.editor.widget.TempGroupBinding;
import org.kevoree.tools.ui.framework.elements.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author ffouquet
 */
public class KevoreeEditorPanel extends JPanel {

    private KevoreeUIKernel kernel = new KevoreeUIKernel();

    public KevoreeUIKernel getKernel() {
        return kernel;
    }

    private KevoreeTypeEditorPanel newL;

    private SourceListSelectionListener previousListener = new SourceListSelectionListener() {
        @Override
        public void sourceListItemSelected(SourceListItem sourceListItem) {
            int divider = splitPane.getDividerLocation() ;
            newL = new KevoreeTypeEditorPanel(palette.getSelectedPanel(), kernel);
            splitPane.setBottomComponent(newL);
            splitPane.setDividerLocation(divider);
        }
    };

    public void setTypeEditor() {
        editableModelPanel.undisplayProperties();
        palette.sourceList().addSourceListSelectionListener(previousListener);
        newL = new KevoreeTypeEditorPanel(palette.getSelectedPanel(), kernel);
        splitPane.setBottomComponent(newL);
    }

    public void unsetTypeEditor() {
        palette.sourceList().removeSourceListSelectionListener(previousListener);
        splitPane.setBottomComponent(editableModelPanel);
    }

    public KevoreeTypeEditorPanel getTypeEditorPanel() {
        return newL;
    }

    private JXPanel leftpanel = new JXPanel();
    //private JXPanel southpanel = new JXPanel();
    private TypeDefinitionSourceList palette = null;


    private EditableModelPanel editableModelPanel = null;
    private JSplitPane splitPane = null;

    public TypeDefinitionSourceList getPalette() {
        return palette;
    }
    //private CommandPanel commandPanel;

    public KevoreeEditorPanel() {
        kernel.setEditorPanel(this);

        leftpanel.setOpaque(false);
        //southpanel.setOpaque(false);

        leftpanel.setLayout(new BorderLayout());
        GradientPaint grad = new GradientPaint(new Point(0, 0), new Color(60, 60, 60), new Point(0, getHeight()), new Color(51, 51, 51));
        MattePainter matte = new MattePainter(grad);
        CompoundPainter p = new CompoundPainter(matte);
        leftpanel.setBackgroundPainter(p);

        this.setLayout(new BorderLayout());

        JScrollPane scrollpane = new JScrollPane();
        IAppWidgetFactory.makeIAppScrollPane(scrollpane);
        editableModelPanel = new EditableModelPanel(scrollpane);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftpanel, editableModelPanel);

        palette = new TypeDefinitionSourceList(splitPane, kernel);


        //   splitPane.setResizeWeight(1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(0);
        splitPane.setDividerLocation(180);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(null);


        scrollpane.setOpaque(false);
        //scrollpane.setPreferredSize(new Dimension(200, 400));
        //scrollpane.setLayout(new ScrollPaneLayout());
        scrollpane.getViewport().add(kernel.getModelPanel());


        //scrollpane.setAutoscrolls(true);


        // this.add(editableModelPanel, BorderLayout.CENTER);

        /* LEFT BAR GENERATION */
        //commandPanel = new CommandPanel(kernel);
        //TrashPanel trash = new TrashPanel();
        leftpanel.add(palette.getComponent(), BorderLayout.CENTER);
        //leftpanel.add(commandPanel, BorderLayout.NORTH);
        //leftpanel.add(trash);

        // this.add(leftpanel, BorderLayout.WEST);
        //this.add(southpanel, BorderLayout.SOUTH);
        //southpanel.setVisible(false);


        /*
  List children =
Arrays.asList(new MultiSplitLayout.Leaf("left"),
      new MultiSplitLayout.Divider(),
      new MultiSplitLayout.Leaf("right"));
MultiSplitLayout.Split modelRoot = new MultiSplitLayout.Split();
modelRoot.setChildren(children);

JXMultiSplitPane multiSplitPane = new JXMultiSplitPane();
multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
multiSplitPane.add(leftpanel, "left");
multiSplitPane.add(editableModelPanel, "right");
        */


        this.add(splitPane, BorderLayout.CENTER);


    }

    /*
    public void loadLib(String uri) {
    ContainerRoot nroot = Art2XmiHelper.load(uri);
    kernel.getModelHandler().merge(nroot);
    palette.clear();
    for (org.kevoree.ComponentTypeLibrary ctl : kernel.getModelHandler().getActualModel().getLibrariy()) {
    for (org.kevoree.ComponentType ct : ctl.getSubComponentTypes()) {
    ComponentTypePanel ctp = kernel.getUifactory().createComponentTypeUI(ct);
    palette.addComponentTypePanel(ctp, ctl.getName());
    }
    }
    this.doLayout();
    repaint();
    revalidate();
    //TODO CLEAN PALETTE

    //Art2XmiHelper.save("/Users/ffouquet/NetBeansProjects/Entimid/org.entimid.fakeStuff/art2Merged.xmi", kernel.getModelHandler().getActualModel());
    }*/
    public void showPropertyFor(Object p) {
        if (p instanceof NodePanel) {
            org.kevoree.ContainerNode elem = (org.kevoree.ContainerNode) kernel.getUifactory().getMapping().get(p);
            NodePropertyEditor prop = new NodePropertyEditor(elem, kernel);
            editableModelPanel.displayProperties(prop);
        }
        if (p instanceof ComponentPanel || p instanceof ChannelPanel || p instanceof GroupPanel) {
            org.kevoree.Instance elem = (org.kevoree.Instance) kernel.getUifactory().getMapping().get(p);
            InstancePropertyEditor prop = new InstancePropertyEditor(elem, kernel);
            editableModelPanel.displayProperties(prop);
        }
        if (p instanceof Binding) {
            Object obj = kernel.getUifactory().getMapping().get(p);
            if(obj instanceof MBinding){
                MBinding elem = (MBinding) obj;
                BindingPropertyEditor prop = new BindingPropertyEditor(elem, kernel);
                editableModelPanel.displayProperties(prop);
            }
            if(obj instanceof TempGroupBinding){
                TempGroupBinding elem = (TempGroupBinding) obj;
                GroupBindingPropertyEditor prop = new GroupBindingPropertyEditor(elem, kernel);
                editableModelPanel.displayProperties(prop);
            }
        }
        this.invalidate();

    }

    public void unshowPropertyEditor() {
        editableModelPanel.undisplayProperties();

    }
}

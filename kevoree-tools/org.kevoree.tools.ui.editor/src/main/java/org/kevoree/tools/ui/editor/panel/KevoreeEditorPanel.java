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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.property.InstancePropertyEditor;
import org.kevoree.tools.ui.editor.property.NodePropertyEditor;
import org.kevoree.tools.ui.framework.elements.ChannelPanel;
import org.kevoree.tools.ui.framework.elements.ComponentPanel;
import org.kevoree.tools.ui.framework.elements.EditableModelPanel;
import org.kevoree.tools.ui.framework.elements.NodePanel;

/**
 *
 * @author ffouquet
 */
public class KevoreeEditorPanel extends JPanel {

    private KevoreeUIKernel kernel = new KevoreeUIKernel();

    public KevoreeUIKernel getKernel() {
        return kernel;
    }
    private JXPanel leftpanel = new JXPanel();
    //private JXPanel southpanel = new JXPanel();
    private TypeDefinitionPalette palette = new TypeDefinitionPalette();


    private EditableModelPanel editableModelPanel = null;

    public TypeDefinitionPalette getPalette() {
        return palette;
    }
    private CommandPanel commandPanel;

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
        scrollpane = new JScrollPane();
        scrollpane.setOpaque(false);
        //scrollpane.setPreferredSize(new Dimension(200, 400));
        //scrollpane.setLayout(new ScrollPaneLayout());
        scrollpane.getViewport().add(kernel.getModelPanel());

        editableModelPanel = new EditableModelPanel(scrollpane);

        //scrollpane.setAutoscrolls(true);


        this.add(editableModelPanel, BorderLayout.CENTER);

        /* LEFT BAR GENERATION */
        commandPanel = new CommandPanel(kernel);
        TrashPanel trash = new TrashPanel();
        leftpanel.add(palette, BorderLayout.CENTER);
        leftpanel.add(commandPanel, BorderLayout.NORTH);
        //leftpanel.add(trash);

        this.add(leftpanel, BorderLayout.WEST);
        //this.add(southpanel, BorderLayout.SOUTH);
        //southpanel.setVisible(false);

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
    public void showPropertyFor(JPanel p) {
       // southpanel.setVisible(true);
       // southpanel.removeAll();
        if (p instanceof NodePanel) {
            org.kevoree.ContainerNode elem = (org.kevoree.ContainerNode) kernel.getUifactory().getMapping().get(p);
            NodePropertyEditor prop = new NodePropertyEditor(elem, kernel);
            //southpanel.add(prop);
            editableModelPanel.displayProperties(prop);
        }
        if (p instanceof ComponentPanel || p instanceof ChannelPanel) {
            org.kevoree.Instance elem = (org.kevoree.Instance) kernel.getUifactory().getMapping().get(p);
            InstancePropertyEditor prop = new InstancePropertyEditor(elem, kernel);
            //southpanel.add(prop);
            editableModelPanel.displayProperties(prop);
        }
       // southpanel.repaint();
       // southpanel.revalidate();


        this.invalidate();

    }

    public void unshowPropertyEditor() {
        editableModelPanel.undisplayProperties();

    }
}

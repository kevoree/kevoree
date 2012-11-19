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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.javase.adaptationsuperviser;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;
import org.kevoree.annotation.*;
//import org.kevoree.framework.KevoreeMessage;
//import org.kevoree.framework.MessagePort;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.adaptationsuperviser.modelview.KevoreeModelViewerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;


/**
 * A Kevoree component that implements a graphical model adaptation superviser.
 * It allows to see, model adaptation and play/pause incoming adaptation request
 * @author dvojtise
 */
@Provides({
//        @ProvidedPort(name = "showText", type = PortType.MESSAGE)
})
@Requires({
//        @RequiredPort(name = "textEntered", type = PortType.MESSAGE, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "allowStepByStep", defaultValue = "false", optional = true)
})
@Library(name = "JavaSE")
@ComponentType
public class AdaptationSuperviser extends AbstractComponentType implements ModelListener {
    public static final Logger logger = LoggerFactory.getLogger(AdaptationSuperviser.class);

    // graphical elements
    public static final int FRAME_WIDTH = 500;
    public static final int FRAME_HEIGHT = 600;
    private ModelHistoryTextLogFrame textLogFrame = null;
    private KevoreeModelViewerPanel currentModelViewPanel = null;
    private JFrame rootFrame = null;
    private JChromeTabbedPane tabbedPane = null;
    private HashMap<String, Tab> tabs = new HashMap<String, Tab>();
    private JPanel tabContent = null;
    private PlayFrame playFrame = null;


    private Thread guiThread = null;


    @Start
    public void start() throws Exception {

        // register to the event
        getModelService().registerModelListener(this);

        initGUI();

    }


    @Stop
    public void stop() {
        // make sure to unregister listener
        getModelService().unregisterModelListener(this);

        // cleanup
        releaseGUI();
    }

    @Update
    public void update() {
        releaseGUI();
        initGUI();
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        textLogFrame.appendModelEvent("preUpdate");
        // todo ignore step if it implies a change on self
        if(playFrame != null){
            playFrame.step();
        }
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        textLogFrame.appendModelEvent("initUpdate");

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        textLogFrame.appendModelEvent("afterLocalUpdate");
        currentModelViewPanel.updateModel(proposedModel);
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modelUpdated() {
        textLogFrame.appendModelEvent("modelUpdated");


    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
        textLogFrame.appendModelEvent("preRollback");
    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
        textLogFrame.appendModelEvent("postRollback");
    }



    private void initGUI(){

        // initialize GUI
        rootFrame = new JFrame("AdaptationSuperviser @@@" + getNodeName());
        rootFrame.setPreferredSize(new Dimension(AdaptationSuperviser.FRAME_WIDTH, AdaptationSuperviser.FRAME_HEIGHT));
        // Tab content
        tabContent = new JPanel();
        tabContent.setLayout(new BorderLayout());
        rootFrame.add(tabContent, BorderLayout.CENTER);

        // TAB
        tabbedPane = new JChromeTabbedPane("osx",tabContent);
        tabbedPane.setPreferredSize(new Dimension(50, 40));
        tabbedPane.getSize(tabbedPane.getPreferredSize());
        if (System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            tabbedPane.setPaintBackground(false);
        }
        rootFrame.add(tabbedPane, BorderLayout.NORTH);

        // TextLog Tab
        textLogFrame = new ModelHistoryTextLogFrame();
        textLogFrame.appendSystem("/***** CONSOLE INITIALIZED  ********/ ");
        Tab textLogTab = tabbedPane.addTab("Event log view");
        textLogTab.setInternPanel(textLogFrame);
        textLogTab.setSelected(true);
        tabs.put("textLogTab", textLogTab);


        // CurrentModel Tab
        currentModelViewPanel =  new KevoreeModelViewerPanel();
        Tab currentModelViewTab = tabbedPane.addTab("Current model view");
        currentModelViewTab.setInternPanel(currentModelViewPanel);
        currentModelViewTab.setSelected(true);
        tabs.put("currentModelViewTab", currentModelViewTab);

        //rootFrame.setContentPane(textLogFrame);

        rootFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        rootFrame.pack();
        rootFrame.setVisible(true);

        //KevoreeLayout.getInstance().displayTab((JPanel)textLogFrame,getName());
        if(Boolean.valueOf((String)getDictionary().get("allowStepByStep"))) {
            // add stepByStep, play and pause buttons
            playFrame = new PlayFrame();
            rootFrame.add(playFrame, BorderLayout.SOUTH);
        }
    }
    private void releaseGUI(){
        guiThread.interrupt();
        guiThread = null;
        rootFrame.dispose();
        rootFrame = null;
        textLogFrame = null;
        playFrame = null;
        tabbedPane = null;
        tabContent = null;
        tabs.clear();
    }


    /*public static void main(String[] args) {
        FakeConsole console = null;
        try {
            console = new FakeConsole();
            console.start();
            console.appendSystem("Ceci est un append system");
            Thread.sleep(2 * 1000);
            console.appendIncoming("Ceci est un message entrant");
            Thread.sleep(2 * 1000);
            console.appendOutgoing("Ceci est un message sortant");
            Thread.sleep(1 * 60 * 1000);
            console.stop();
        } catch (InterruptedException ex) {
            if (console != null) {
                //console.getLoggerLocal().error(ex.getClass().getSimpleName(), ex);
            } else {
                System.out.println(ex.toString());
            }
        }

    }*/
}

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

package org.kevoree.tools.ui.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.ui.editor.command.LoadModelCommand;
import org.kevoree.tools.ui.editor.command.SaveActuelModelCommand;
import org.kevoree.tools.ui.editor.panel.KevoreeEditorPanel;

/**
 *
 * @author ffouquet
 */
public class KevoreeEditor {

    private KevoreeEditorPanel panel = null;
    private KevoreeMenuBar menubar = null;

    public KevoreeEditor(){
        panel = new KevoreeEditorPanel();
        menubar = new KevoreeMenuBar(panel.getKernel());
    }

    public JPanel getPanel(){
        return panel;
    }

    public JMenuBar getMenuBar(){
        return menubar;
    }

    public void loadModel(String uri){
        LoadModelCommand command = new LoadModelCommand();
        command.setKernel(panel.getKernel());
        command.execute(uri);
    }

    public void loadLib(){
        //TODO
    }

    public void saveModel(String url){
        KevoreeXmiHelper.save(url, panel.getKernel().getModelHandler().getActualModel());
    }

    public void setDefaultSaveLocation(String url){
        SaveActuelModelCommand.setDefaultLocation(url);
    }

    public String getEditorVersion() {

        InputStream is = getClass().getResourceAsStream("/META-INF/maven/org.kevoree.tools/org.kevoree.tools.ui.editor/pom.properties");
        //System.out.println("VErsion ???"+is);

        String version = null;

        if(is != null) {
            try {
                Properties p = new Properties();
                p.load(is);
                version = p.getProperty("version");
            } catch (IOException ex) {
                Logger.getLogger(KevoreeEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(KevoreeEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(version == null) {
            return "";
        } else {
            return version;
        }

    }

}

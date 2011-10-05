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
/* $Id: SaveActuelModelCommand.java 13868 2010-12-14 22:06:18Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import javax.swing.JFileChooser;

import org.kevoree.ContainerNode;
import org.kevoree.framework.KevoreeXmiHelper;

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.PositionedEMFHelper;
import org.kevoree.tools.ui.framework.elements.NodePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ffouquet
 */
public class SaveActuelModelCommand implements Command {

	Logger logger = LoggerFactory.getLogger(SaveActuelModelCommand.class);

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    private KevoreeUIKernel kernel;
    private JFileChooser filechooser = new JFileChooser();
    private static String defaultLocation = null;

    public static String getDefaultLocation() {
        return defaultLocation;
    }

    public static void setDefaultLocation(String uri) {
        defaultLocation = uri;
    }

    @Override
    public void execute(Object p) {

        PositionedEMFHelper.updateModelUIMetaData(kernel);

        String location = "";
        if (defaultLocation == null) {
            filechooser.showSaveDialog(kernel.getModelPanel());
            if (filechooser.getSelectedFile() != null) {
                location = filechooser.getSelectedFile().getPath();
            }
        } else {
            location = defaultLocation;
        }
        try {
            KevoreeXmiHelper.save(location.toString(), kernel.getModelHandler().getActualModel());
        } catch (Exception e) {
            logger.error("Can't save model !", e);
        }

    }
}

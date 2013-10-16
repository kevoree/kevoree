/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.kevoree.serializer.JSONModelSerializer;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.PositionedEMFHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author ffouquet
 */
public class SaveActuelModelJSONCommand implements Command {

	Logger logger = LoggerFactory.getLogger(SaveActuelModelJSONCommand.class);

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    private KevoreeUIKernel kernel;
    private JFileChooser filechooser = new JFileChooser();
    private static File defaultLocation = null;
    private static String previousLocation = null;

    @Override
    public void execute(Object p) {

        PositionedEMFHelper.updateModelUIMetaData(kernel);

        if (defaultLocation == null) {
            if(previousLocation != null) {
                filechooser.setSelectedFile(new File(previousLocation));
            }
            int result = filechooser.showSaveDialog(kernel.getModelPanel());
            if (filechooser.getSelectedFile() != null && result == JFileChooser.APPROVE_OPTION) {
                doSave(filechooser.getSelectedFile());
            }
        } else {
            doSave(defaultLocation);

        }

    }

    private void doSave(File location) {
        try {
            FileOutputStream oo = new FileOutputStream(location);
            JSONModelSerializer saver = new JSONModelSerializer();
            saver.serializeToStream(kernel.getModelHandler().getActualModel(),oo);
            previousLocation = location.getAbsolutePath();
        } catch (Exception e) {
            logger.error("Can't save model to default location !", e);
        }
    }



}

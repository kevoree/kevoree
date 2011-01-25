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
/* $Id: LoadNewLibCommand.java 13269 2010-11-02 14:24:49Z hrambelo $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

import org.eclipse.emf.common.util.URI;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;

/**
 *
 * @author ffouquet
 */
public class LoadNewLibCommand implements Command {

    private JFileChooser filechooser = new JFileChooser();

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }
    private KevoreeUIKernel kernel;

    @Override
    public void execute(Object p) {
        filechooser.showOpenDialog(kernel.getModelPanel());
        if (filechooser.getSelectedFile() != null) {
            JarFile jar;
            try {

                jar = new JarFile(filechooser.getSelectedFile().getAbsoluteFile()); //new JarFile(filechooser.getSelectedFile().getAbsoluteFile().toURI().toString());
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                if (entry != null) {
                    String path = convertStreamToFile(jar.getInputStream(entry));
                    //kernel.getEditorPanel().loadLib(path);
                    //System.out.println(path);

                    //Load
                    ContainerRoot nroot = KevoreeXmiHelper.load(path);

                    //Merge
                    kernel.getModelHandler().merge(nroot);

                    //CREATE TEMP FILE FROM ACTUAL MODEL
                    File tempFile = File.createTempFile("kevoreeEditorTemp", ".kev");
                    KevoreeXmiHelper.save(URI.createFileURI(tempFile.getAbsolutePath()).toString(),kernel.getModelHandler().getActualModel());

                    //LOAD MODEL
                    LoadModelCommand loadCmd = new LoadModelCommand();
                    loadCmd.setKernel(kernel);
                    loadCmd.execute(URI.createFileURI(tempFile.getAbsolutePath()).toString());


                    /*
                    kernel.getEditorPanel().getPalette().clear();
                    for (org.kevoree.TypeLibrary ctl : kernel.getModelHandler().getActualModel().getLibraries()) {
                        for (org.kevoree.TypeDefinition ct : ctl.getSubTypes()) {
                            if (ct instanceof ComponentType) {
                                ComponentTypePanel ctp = kernel.getUifactory().createComponentTypeUI((ComponentType) ct);
                                kernel.getEditorPanel().getPalette().addTypeDefinitionPanel(ctp, ctl.getName());
                            }
                            if (ct instanceof ChannelType) {
                                ChannelTypePanel ctp = kernel.getUifactory().createChannelTypeUI((ChannelType) ct);
                                kernel.getEditorPanel().getPalette().addTypeDefinitionPanel(ctp, ctl.getName());
                            }
                        }
                    }
                    kernel.getEditorPanel().doLayout();
                    kernel.getEditorPanel().repaint();
                    kernel.getEditorPanel().revalidate();
*/


                }
            } catch (IOException ex) {
                Logger.getLogger(LoadNewLibCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String convertStreamToFile(InputStream inputStream) throws IOException {
        Random rand = new Random();
        File temp = File.createTempFile("kevoreeloaderLib" + rand.nextInt(), ".xmi");
        // Delete temp file when program exits.
        temp.deleteOnExit();
        OutputStream out = new FileOutputStream(temp);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        inputStream.close();
        out.flush();
        out.close();

        return URI.createFileURI(temp.getAbsolutePath()).toString();
    }
}

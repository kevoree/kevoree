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
package org.kevoree.tools.ui.editor.command;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.UIEventHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * User: ffouquet
 * Date: 15/06/11
 * Time: 23:16
 */
public class ExportModelSVGImage implements Command {
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


        filechooser.showSaveDialog(kernel.getModelPanel());

        if (filechooser.getSelectedFile() != null) {
            try {
                UIEventHandler.info("generating SVG file !");
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = domImpl.createDocument(svgNS, "svg", null);
                SVGGraphics2D g2 = new SVGGraphics2D(document);
                kernel.getModelPanel().paintComponents(g2);
                kernel.getModelPanel().paint(g2);
                FileOutputStream fout = new FileOutputStream(filechooser.getSelectedFile());
                Writer out = new OutputStreamWriter(fout, "UTF-8");
                g2.stream(out, true);
                fout.close();
                UIEventHandler.info("SVGG generation complete !");
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }
}

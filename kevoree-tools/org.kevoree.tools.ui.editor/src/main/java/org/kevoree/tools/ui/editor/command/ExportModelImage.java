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

import org.kevoree.tools.ui.editor.KevoreeUIKernel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: ffouquet
 * Date: 15/06/11
 * Time: 23:16
 */
public class ExportModelImage implements Command {
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

                Dimension size = kernel.getModelPanel().getSize();
                BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D g2 = bi.createGraphics();

                kernel.getModelPanel().paintComponents(g2);
                kernel.getModelPanel().paint(g2);


                ImageIO.write(bi, "PNG", filechooser.getSelectedFile().getAbsoluteFile());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }
}

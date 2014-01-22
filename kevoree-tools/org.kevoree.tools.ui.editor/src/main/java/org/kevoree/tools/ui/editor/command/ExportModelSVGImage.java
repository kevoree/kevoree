///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.kevoree.tools.ui.editor.command;
//
//import org.kevoree.tools.ui.editor.KevoreeUIKernel;
//import org.kevoree.tools.ui.editor.UIEventHandler;
//import org.w3c.dom.DOMImplementation;
//import org.w3c.dom.Document;
//import javax.swing.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStreamWriter;
//import java.io.Writer;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * User: ffouquet
// * Date: 15/06/11
// * Time: 23:16
// */
//public class ExportModelSVGImage implements Command, Runnable {
//    public void setKernel(KevoreeUIKernel kernel) {
//        this.kernel = kernel;
//    }
//
//    public ExportModelSVGImage() {
//    }
//
//    private File selectedFile = null;
//
//    public ExportModelSVGImage(KevoreeUIKernel k ,File f) {
//        selectedFile = f;
//        kernel = k ;
//    }
//
//
//    private KevoreeUIKernel kernel;
//    private JFileChooser filechooser = new JFileChooser();
//    private static String defaultLocation = null;
//
//    public static String getDefaultLocation() {
//        return defaultLocation;
//    }
//
//    public static void setDefaultLocation(String uri) {
//        defaultLocation = uri;
//    }
//
//    private ExecutorService pool = Executors.newSingleThreadExecutor();
//
//    @Override
//    public void execute(Object p) {
//        filechooser.showSaveDialog(kernel.getModelPanel());
//        if (filechooser.getSelectedFile() != null) {
//            pool.submit(new ExportModelSVGImage(kernel,filechooser.getSelectedFile()));
//            UIEventHandler.info("generating SVG file !");
//        }
//
//    }
//
//
//    @Override
//    public void run() {
//        try {
//            kernel.getModelPanel().clearBuffer();
//            DOMImplementation impl =
//                    GenericDOMImplementation.getDOMImplementation();
//            String svgNS = "http://www.w3.org/2000/svg";
//            Document myFactory = impl.createDocument(svgNS, "svg", null);
//            SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
//            ctx.setEmbeddedFontsOn(true);
//            SVGGraphics2D g2 = new SVGGraphics2D(ctx,true);
//            kernel.getModelPanel().paintComponents(g2);
//            kernel.getModelPanel().paint(g2);
//            FileOutputStream fout = new FileOutputStream(selectedFile);
//            Writer out = new OutputStreamWriter(fout, "UTF-8");
//            g2.stream(out, true);
//            fout.close();
//            UIEventHandler.info("SVG generation complete !");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//

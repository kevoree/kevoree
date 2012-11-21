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
package org.kevoree.tools.ui.editor.command;

import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.prefuse.KevoreePrefuseGraph;
import org.kevoree.tools.ui.editor.command.prefuse.PrefuseGraphView;
import prefuse.activity.Activity;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.VisualItem;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Arrange all graphical elements with a simple layout algorithm
 */
public class ArrangeAllLayoutCommand implements Command  {

    public ExecutorService executor = Executors.newFixedThreadPool(1);

    public boolean mustDrawPrefuse = false;
    public long stepToRun = 250;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    private KevoreeUIKernel kernel;

    @Override
    public void execute(Object p) {

        ContainerRoot modelToArrange = kernel.getModelHandler().getActualModel();
        final KevoreePrefuseGraph kpg = new KevoreePrefuseGraph(kernel);
        kpg.computePrefuseGraph(modelToArrange);

        // create initial view for g using real position of the effective panel
        final PrefuseGraphView view = new PrefuseGraphView(kpg,
                ((Double)(kernel.getModelPanel().getWidth()/KevoreePrefuseGraph.DISPLAY_SCALE_FACTOR)).intValue(),
                ((Double)(kernel.getModelPanel().getHeight()/KevoreePrefuseGraph.DISPLAY_SCALE_FACTOR)).intValue(),
                mustDrawPrefuse,
                stepToRun );

        // launch the computation in background
        //view.getVisualization().run("draw");
        view.run();
        // show in separate frame
        if(mustDrawPrefuse){
            JFrame frame = new JFrame("p r e f u s e  |  g r a p h v i e w");
            frame.setContentPane(view);
            frame.pack();
            frame.setVisible(true);
        }

        executor.execute(new Runnable(){
            @Override
            public void run() {
                // compute next position using prefuse
                for (int i = 0; i < stepToRun; i++){
                    try{
                        // let prefuse work a little before getting the result
                        Thread.sleep((long) (Activity.DEFAULT_STEP_TIME));
                        kpg.updateCurrentLocationFromPrefuse(view);

                        //REFRESH Kevoree editor UI
                        kernel.getEditorPanel().doLayout();
                        kernel.getEditorPanel().repaint();
                    }
                    // update panel positions
                    catch(InterruptedException ie){}
                }
                view.cancel();
            }
        });



    }




}

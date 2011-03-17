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
/* $Id: SaveActuelModelCommand.java 13086 2010-10-21 11:40:48Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.editor.component.creator.commands;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 *
 * @author ffouquet
 */
public class NewModelCommand extends Command {

    private JFileChooser filechooser = new JFileChooser();
    private static String defaultLocation = null;

    public static void setDefaultLocation(String uri) {
        defaultLocation = uri;
    }

    @Override
    public void execute(Object p) {
        boolean resume = true;
        if (kernel.getModelHandler().containsUnsavedChanges()) {
            int answer = JOptionPane.showConfirmDialog(null, "Il existe des modifications non enregistrées. Poursuivre ?", "Attention", JOptionPane.YES_NO_OPTION);
            resume = (answer == JOptionPane.YES_OPTION);
        }
        if (resume) {
            kernel.getModelHandler().newModel();
            kernel.getModelPanel().clearAll();
        }
    }
}

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

package org.kevoree.editor.component.creator.commands;

import java.awt.Point;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.model.LibraryModelElement;

/**
 *
 * @author gnain
 */
public class CreateLibraryCommand extends Command {
    
    private Point p;

    public void setPoint(Point p) {
        this.p = p;
    }

    public void execute(Object o) {
       LibraryModelElement modelElement = kernel.getModelHandler().addLibrary();
       modelElement.setLocation(p);
        kernel.getModelPanel().addLibrary(modelElement.getGraphicalRepresentation());
        kernel.getModelPanel().revalidate();
        kernel.getModelPanel().repaint();
    }
}

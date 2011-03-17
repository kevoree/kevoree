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

package org.kevoree.editor.component.creator.listeners;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.model.Art2ModelElement;

/**
 *
 * @author gnain
 */
public class TransferableArt2ModelElement implements Transferable {

    public static DataFlavor graphicalElementFlavor = new DataFlavor(JPanel.class, "Art2GraphicalElement");

    protected DataFlavor[] acceptedFlavours = new DataFlavor[]{
        graphicalElementFlavor,
        DataFlavor.stringFlavor
    };

    private Art2ModelElement element;

    public TransferableArt2ModelElement(Art2ModelElement e) {
        element = e;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return acceptedFlavours;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor df) {
        return (df==graphicalElementFlavor||df==DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if(df==graphicalElementFlavor){
            return element.getGraphicalRepresentation();
        } else if(df==DataFlavor.stringFlavor) {
            return element.getClass().getName();
        } else {
            throw new UnsupportedFlavorException(df);
        }
    }

}

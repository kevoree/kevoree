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
import org.eclipse.emf.common.command.Command;
import org.kevoree.editor.component.creator.palettes.Art2Element;

/**
 *
 * @author gnain
 */
public class TransferableArt2Element implements Transferable {

    public static DataFlavor graphicalElementFlavor = new DataFlavor(JPanel.class, "Art2GraphicalElement");
    public static DataFlavor art2ElementFlavor = new DataFlavor(Art2Element.class, "Art2Element");

    protected DataFlavor[] acceptedFlavours = new DataFlavor[]{
        graphicalElementFlavor,
        art2ElementFlavor,
        DataFlavor.stringFlavor
    };

    private Art2Element element;

    public TransferableArt2Element(Art2Element e) {
        element = e;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return acceptedFlavours;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor df) {
        return (df==graphicalElementFlavor||df==art2ElementFlavor||df==DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if(df==graphicalElementFlavor){
            return element.getGraphicalElement();
        } else if(df==art2ElementFlavor) {
            return element;
        } else if(df==DataFlavor.stringFlavor) {
            return element.getElementName();
        } else {
            throw new UnsupportedFlavorException(df);
        }
    }

}

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
package org.kevoree.tools.ui.framework.listener;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * User: ffouquet
 * Date: 16/06/11
 * Time: 22:24
 */
public class TypeDefinitionTransferHandler extends TransferHandler {

    protected Transferable createTransferable(JComponent c) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
                return true;
            }

            @Override
            public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
                return null;
            }
        };
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

}

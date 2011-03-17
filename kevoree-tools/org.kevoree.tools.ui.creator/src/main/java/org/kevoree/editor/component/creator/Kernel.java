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

package org.kevoree.editor.component.creator;

import org.kevoree.editor.component.creator.handlers.ModelHandler;
import org.kevoree.editor.component.creator.handlers.ModelMapper;
import org.kevoree.editor.component.creator.panels.BasicCommandsPanel;
import org.kevoree.editor.component.creator.panels.ModelPanel;
import org.kevoree.editor.component.creator.panels.PalettePanel;

/**
 *
 * @author gnain
 */
public class Kernel {

    private ModelPanel modelPanel;
    private PalettePanel palette;
    private ModelHandler modelHandler;
    private BasicCommandsPanel basicCommandsPanel;
    private ModelMapper modelMapper;

    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public BasicCommandsPanel getBasicCommandsPanel() {
        return basicCommandsPanel;
    }

    public void setBasicCommandsPanel(BasicCommandsPanel basicCommandsPanel) {
        this.basicCommandsPanel = basicCommandsPanel;
    }

    public ModelHandler getModelHandler() {
        return modelHandler;
    }

    public void setModelHandler(ModelHandler modelHandler) {
        this.modelHandler = modelHandler;
    }

    public ModelPanel getModelPanel() {
        return modelPanel;
    }

    public void setModelPanel(ModelPanel modelPanel) {
        this.modelPanel = modelPanel;
    }

    public PalettePanel getPalette() {
        return palette;
    }

    public void setPalette(PalettePanel palette) {
        this.palette = palette;
    }

}

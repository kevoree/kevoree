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
package org.kevoree.kompare;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.kompare.Art2KompareBean;
import org.kevoreeAdaptation.AdaptationModel;

/**
 *
 * @author ffouquet
 */
@Provides({
    @ProvidedPort(name = "kompareService", type = PortType.SERVICE, className = org.kevoree.api.service.core.kompare.ModelKompareService.class)
})
@Library(name = "ART2Core")
@ComponentType
public class Art2KompareComponent extends AbstractComponentType {

    private Art2KompareBean bean = new Art2KompareBean();

    @Start
    public void start() {
    }

    @Stop
    public void stop() {
    }

    @Port(name="kompareService",method="kompare")
    public AdaptationModel kompare(ContainerRoot actualModel, ContainerRoot targetModel, String nodeName) {
        return bean.kompare(actualModel, targetModel, nodeName);
    }
}

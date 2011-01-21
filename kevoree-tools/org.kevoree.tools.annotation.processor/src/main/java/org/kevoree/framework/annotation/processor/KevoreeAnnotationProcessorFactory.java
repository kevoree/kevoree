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
package org.kevoree.framework.annotation.processor;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Port;
import org.kevoree.annotation.Ports;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.annotation.RequiredPort;
import org.kevoree.annotation.Requires;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.ThirdParties;
import org.kevoree.annotation.ThirdParty;

/**
 *
 * @author ffouquet
 */
public class KevoreeAnnotationProcessorFactory implements
        AnnotationProcessorFactory {

    @Override
    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> supportedAnnotationTypes() {
        Collection<String> stype = new ArrayList();
        stype.add(ChannelTypeFragment.class.getName());
        stype.add(ComponentType.class.getName());
        stype.add(Port.class.getName());
        stype.add(ProvidedPort.class.getName());
        stype.add(Provides.class.getName());
        stype.add(Requires.class.getName());
        stype.add(RequiredPort.class.getName());
        stype.add(Start.class.getName());
        stype.add(Stop.class.getName());
        stype.add(Ports.class.getName());
        stype.add(ThirdParties.class.getName());
        stype.add(ThirdParty.class.getName());
        stype.add(DictionaryAttribute.class.getName());
        stype.add(DictionaryType.class.getName());
        stype.add(ComponentFragment.class.getName());
        stype.add(Library.class.getName());
        return stype;
    }

    @Override
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations, AnnotationProcessorEnvironment env) {

        // System.out.println("hello");

        AnnotationProcessor result;

        if (declarations.isEmpty()) {
            result = AnnotationProcessors.NO_OP;
        } else {
            result = new org.kevoree.framework.annotation.processor.visitor.KevoreeAnnotationProcessor(env);
        }
        return result;
    }
}

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
package org.kevoree.framework.annotation.processor.visitor;

import org.kevoree.KevoreeFactory;
import org.kevoree.Operation;
import org.kevoree.Parameter;
import org.kevoree.ServicePortType;
import org.kevoree.framework.annotation.processor.LocalUtility;
import org.kevoree.impl.DefaultKevoreeFactory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ffouquet
 */
public class ServicePortTypeVisitor extends SimpleTypeVisitor6<Object, Object> {

    protected KevoreeFactory kevoreeFactory = new DefaultKevoreeFactory();
    ServicePortType dataType = kevoreeFactory.createServicePortType();

    private ServicePortType currentFirstType;
    private List<ServicePortType> inheritedDataTypes;

    public ServicePortType getDataType() {
        return dataType;
    }


    public List<ServicePortType> getInheritedDataTypes() {
        return inheritedDataTypes;
    }

    public void setDataType(ServicePortType dataType) {
        this.dataType = dataType;
    }

    public void visitTypeDeclaration(DeclaredType dt) {
        ServicePortType newServicePortType;
        if (inheritedDataTypes == null) {
            newServicePortType = dataType;
        } else {
            newServicePortType = kevoreeFactory.createServicePortType();
        }

        newServicePortType.setName(dt.asElement().toString());
        for (Element e : dt.asElement().getEnclosedElements()) {
            ExecutableElement ee = (ExecutableElement) e;
            if (e.getKind().compareTo(ElementKind.METHOD) == 0) {
                Operation newo = kevoreeFactory.createOperation();
                newo.setName(e.getSimpleName().toString());
                newServicePortType.addOperations(newo);
                //BUILD RETURN TYPE
                DataTypeVisitor rtv = new DataTypeVisitor();
                ee.getReturnType().accept(rtv, ee.getReturnType());
                newo.setReturnType(LocalUtility.getOraddDataType(rtv.getDataType()));
                //BUILD PARAMETER
                Integer i = 0;
                for (VariableElement ve : ee.getParameters()) {
                    Parameter newp = kevoreeFactory.createParameter();
                    newp.setName(ve.toString());
                    newp.setOrder(i);
                    newo.addParameters(newp);
                    DataTypeVisitor ptv = new DataTypeVisitor();
                    ve.asType().accept(ptv, ve);
                    newp.setType(LocalUtility.getOraddDataType(ptv.getDataType()));
                    i = i + 1;

                }
            }
        }

        if (inheritedDataTypes != null) {
            inheritedDataTypes.add(newServicePortType);
            currentFirstType.addSuperTypes(newServicePortType);
        }
        if (dt.asElement() instanceof TypeElement) {
            currentFirstType = newServicePortType;
            manageSuperTypes(((TypeElement) dt.asElement()));

        }
    }

    private void manageSuperTypes(TypeElement typeElement) {
        if (inheritedDataTypes == null) {
            inheritedDataTypes = new ArrayList<ServicePortType>();
        }
        // MANAGE SUPERCLASS
        TypeMirror superClass = typeElement.getSuperclass();
        if (!superClass.getKind().equals(TypeKind.NONE)) {
            superClass.accept(this, superClass);
        }
        // MANAGE INTERFACES
        for (TypeMirror typeInterface : typeElement.getInterfaces()) {
            typeInterface.accept(this, typeInterface);
        }
    }

    @Override
    public Object visitDeclared(DeclaredType declaredType, Object o) {
        visitTypeDeclaration(declaredType);
        return null;
    }


}

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

import org.kevoree.*;
import org.kevoree.framework.annotation.processor.LocalUtility;
import scala.Some;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;

/**
 * @author ffouquet
 */
public class ServicePortTypeVisitor implements TypeVisitor<Object, Object> {

    ServicePortType dataType = KevoreeFactory.createServicePortType();

    public ServicePortType getDataType() {
        return dataType;
    }

    public void setDataType(ServicePortType dataType) {
        this.dataType = dataType;
    }

    public void visitTypeDeclaration(TypeMirror t) {

        if (t instanceof javax.lang.model.type.DeclaredType) {
            javax.lang.model.type.DeclaredType dt = (javax.lang.model.type.DeclaredType) t;
            dataType.setName(dt.asElement().getSimpleName().toString());
            for (Element e : dt.asElement().getEnclosedElements()) {
                ExecutableElement ee = (ExecutableElement) e;
                if (e.getKind().compareTo(ElementKind.METHOD) == 0) {
                    Operation newo = KevoreeFactory.createOperation();
                    dataType.addOperations(newo);
                    newo.setName(e.getSimpleName().toString());
                    //BUILD RETURN TYPE
                    DataTypeVisitor rtv = new DataTypeVisitor();
                    ee.getReturnType().accept(rtv, ee.getReturnType());
                    


                    newo.setReturnType(new Some<TypedElement>(LocalUtility.getOraddDataType(rtv.getDataType())));


                    //BUILD PARAMETER
                    for (VariableElement ve : ee.getParameters()) {
                        Parameter newp = KevoreeFactory.createParameter();
                        newo.addParameters(newp);
                        newp.setName(ve.getSimpleName().toString());
                        DataTypeVisitor ptv = new DataTypeVisitor();

                        //ve.getType().accept(ptv);
                        //newp.setType(new Some<TypedElement>(LocalUtility.getOraddDataType(ptv.getDataType())));
                    }
                }
            }

        }
    }

    @Override
    public Object visit(TypeMirror typeMirror, Object o) {
        visitTypeDeclaration(typeMirror);
        return null;
    }

    @Override
    public Object visit(TypeMirror typeMirror) {
        visitTypeDeclaration(typeMirror);
        return null;
    }

    @Override
    public Object visitPrimitive(PrimitiveType primitiveType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitNull(NullType nullType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitArray(ArrayType arrayType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitDeclared(DeclaredType declaredType, Object o) {
        visitTypeDeclaration(declaredType);
        return null;
    }

    @Override
    public Object visitError(ErrorType errorType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitTypeVariable(TypeVariable typeVariable, Object o) {
        dataType.setName(typeVariable.getKind().getDeclaringClass().getCanonicalName());
        return dataType;
    }

    @Override
    public Object visitWildcard(WildcardType wildcardType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitExecutable(ExecutableType executableType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitNoType(NoType noType, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visitUnknown(TypeMirror typeMirror, Object o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

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
package org.kevoree.library.sample;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sample.service.ServicePrimitifReturnTypes;

/**
 *
 * @author gnain
 */
@Provides({
    @ProvidedPort(name = "portPrimitiveTypes", type = PortType.SERVICE, className = ServicePrimitifReturnTypes.class)
})
@Library(name = "Kevoree-Samples")
@ComponentType
public class ComponentPrimitiveTypeService extends AbstractComponentType implements ServicePrimitifReturnTypes {

    @Port(name = "portPrimitiveTypes", method = "methodShort")
    public short methodShort(short s) {
        return (short) 42;
    }

    @Port(name = "portPrimitiveTypes", method = "methodInt")
    public int methodInt(int i) {
        return (int) 42;
    }

    @Port(name = "portPrimitiveTypes", method = "methodLong")
    public long methodLong(long l) {
        return 42L;
    }

    @Port(name = "portPrimitiveTypes", method = "methodDouble")
    public double methodDouble(double d) {
        return 42d;
    }

    @Port(name = "portPrimitiveTypes", method = "methodFloat")
    public float methodFloat(float f) {
        return 42f;
    }

    @Port(name = "portPrimitiveTypes", method = "methodChar")
    public char methodChar(char c) {
        return (char) 'b';
    }

    @Port(name = "portPrimitiveTypes", method = "methodVoid")
    public void methodVoid() {
    }

    @Port(name = "portPrimitiveTypes", method = "methodByte")
    public byte methodByte(byte b) {
        return (byte) 0x42;
    }

    @Port(name = "portPrimitiveTypes", method = "methodBoolean")
    public boolean methodBoolean(boolean b) {
        return true;
    }

    @Start
    public void start() {
        System.out.println("ComponentPrimitiveTypeServce::start");
    }

    @Stop
    public void stop() {
        System.out.println("ComponentPrimitiveTypeServce::stop");
    }
}

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
package org.kevoree.tools.nativeN;


import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.api.NativeListenerPorts;
import org.kevoree.tools.nativeN.generator.AbstractCodeGenerator;
import org.kevoree.tools.nativeN.generator.CodeGeneratorC;
import org.kevoree.tools.nativeN.generator.CodeGeneratorJava;
import org.kevoree.tools.nativeN.utils.KevScriptLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Test {





    public static void main(String[] args) throws Exception {

        ContainerRoot model =  KevScriptLoader.getModel("/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.faceDetection/src/main/kevs/FaceDetection.kevs");
        String path_uexe = "/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.faceDetection/modules/nix32/target/org.kevoree.library.nativeN.faceDetection-wrapper-nix32.uexe";


        int ipc_key = 51516;

        ArrayList<String> repos = new ArrayList<String>();
        repos.add("http://maven.kevoree.org/release/");
        repos.add("http://maven.kevoree.org/snapshots/");

        NativeManager nativeManager = new NativeManager(ipc_key,path_uexe,model);
        nativeManager.addEventListener(new NativeListenerPorts() {

            @Override
            public void disptach(NativeEventPort event, String port_name, String msg) {
                System.out.println("DISPATCH from 1 " + port_name + " =" + msg);
            }
        });


        boolean  started = nativeManager.start();

        if(started)
        {

            nativeManager.update();

        } else
        {
            System.out.println("error");
        }


        Thread.sleep(9000);

        nativeManager.stop();

        System.exit(0);

    }


}

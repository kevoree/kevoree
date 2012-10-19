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
import java.util.ArrayList;

public class Test {





    public static void main(String[] args) throws Exception {


        ContainerRoot model =  KevScriptLoader.getModel("/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.HelloWorld/src/main/HelloWorld.kevs");

        AbstractCodeGenerator codeGeneratorC = new CodeGeneratorC(model);
        codeGeneratorC.execute();

        AbstractCodeGenerator codeGeneratorJava = new CodeGeneratorJava(model);
        codeGeneratorJava.execute();



        System.exit(0);
        int ipc_key = 6819;

        ArrayList<String> repos = new ArrayList<String>();
        repos.add("http://maven.kevoree.org/release/");
        repos.add("http://maven.kevoree.org/snapshots/");

        /*
        <groupId>org.kevoree.library.nativeN</groupId>
        <artifactId>org.kevoree.library.nativeN.HelloWorld_native</artifactId>*/

        /*    File binary =   AetherUtil.resolveMavenArtifact4J("org.kevoree.library.nativeN.HelloWorld_native", "org.kevoree.library.nativeN", "1.8.9-SNAPSHOT", "uexe", repos);

    if(!binary.canExecute())
    {
        binary.setExecutable(true);
    }
        */
        String path_uexe = "/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.HelloWorld/org.kevoree.library.nativeN.HelloWorld_native/nix32/target/org.kevoree.library.nativeN.HelloWorld_native-nix32.uexe";
        // System.out.println(binary.getPath());

        KevScriptLoader kevScriptLoader =new KevScriptLoader();

        //  ContainerRoot model =   kevScriptLoader.getModel("/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.HelloWorld/src/main/HelloWorld.kevs");
        // loading model from jar
        ContainerRoot model2 = KevoreeXmiHelper.load("/home/jed/DAUM_PROJECT/daum-library/pocXenomai/org.kevoree.tools.nativeN/org.kevoree.tools.nativeN.core/src/main/resources/lib.kev");

        NativeManager nativeManager = new NativeManager(ipc_key,"HelloWorld",path_uexe,model);


        nativeManager.addEventListener(new NativeListenerPorts() {

            @Override
            public void disptach(NativeEventPort event, String port_name, String msg) {
                System.out.println("DISPATCH from " + port_name + " =" + msg);
            }
        });


        boolean  started = nativeManager.start();

        if(started)
        {

            for(int i = 0;i <100;i++){
                nativeManager.push("input_port", "hello world " + i);
            }


        } else
        {
            System.out.println("error");
        }

        nativeManager.stop();

        System.exit(0);




    }


}

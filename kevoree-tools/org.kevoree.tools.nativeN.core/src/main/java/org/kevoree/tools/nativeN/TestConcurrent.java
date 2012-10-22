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
import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.api.NativeListenerPorts;
import org.kevoree.tools.nativeN.utils.KevScriptLoader;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/10/12
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class TestConcurrent {


    public static void main(String[] args) throws Exception
    {
        ContainerRoot model =  KevScriptLoader.getModel("/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.HelloWorld/src/main/HelloWorld.kevs");
        String path_uexe = "/home/jed/KEVOREE_PROJECT/kevoree/kevoree-corelibrary/native/org.kevoree.library.nativeN.HelloWorld/org.kevoree.library.nativeN.HelloWorld_native/nix32/target/org.kevoree.library.nativeN.HelloWorld_native-nix32.uexe";
        ContainerRoot model2 = KevScriptLoader.getModel("/home/jed/KEVOREE_PROJECT/kevoree/kevoree-tools/org.kevoree.tools.nativeN.core/src/main/java/org/kevoree/tools/nativeN/HelloWorld.kevs");



        int ipc_key = 215169;

        int ipc_key2 = 2151775;


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
        nativeManager.setDico("myParam","value1");
        if(started)
        {

            nativeManager.update();

        } else
        {
            System.out.println("error");
        }


        NativeManager nativeManager2 = new NativeManager(ipc_key2,path_uexe,model2);


        nativeManager2.addEventListener(new NativeListenerPorts() {

            @Override
            public void disptach(NativeEventPort event, String port_name, String msg) {
                System.out.println("DISPATCH from 2 " + port_name + " =" + msg);
            }
        });


        boolean  started2 = nativeManager2.start();
        nativeManager2.setDico("myParam","value2");
        if(started2)
        {

            nativeManager2.update();

        } else
        {
            System.out.println("error");
        }




        Thread.sleep(9000);

        nativeManager.stop();

        nativeManager2.stop();

   System.exit(0);

    }
}

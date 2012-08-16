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
package org.kevoree.nativeNode;




public class Test {

    public static void main(String[] args) throws Exception {

        int ipc_key = 256181;

        String binary =   "/home/jed/DAUM_PROJECT/daum-library/pocXenomai/org.kevoree.nativeNode/org.kevoree.native.testcomponent/target/org.kevoree.native.testcomponent.uexe";

        final Handler  poc = new Handler(ipc_key,9014,binary);

        poc.create_input("input_port");

        poc.create_output("output_port");


        /*
         System.out.println(poc.GenerateInputsPorts());
        System.out.println(poc.generatorPorts());    */

        poc.addEventListener(new NativePortsListener() {

            @Override
            public void disptach(NativePortEvent event, String port_name, String msg)
            {
                System.out.println("DISPATCH from "+port_name+" ="+msg);
            }
        });

        poc.start();

        poc.update();

        Thread t = new Thread(new Runnable() {
           @Override
           public void run()
           {
               for(int i=0;i<4000;i++)
               {
                   poc.enqueue("input_port","hello world "+i);
               }
           }
       });
        t.start();

        t.join();


        poc.stop();
        System.exit(0);

    }

}

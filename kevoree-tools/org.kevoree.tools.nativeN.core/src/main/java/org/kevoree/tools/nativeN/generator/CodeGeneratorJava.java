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
package org.kevoree.tools.nativeN.generator;

import org.kevoree.ContainerRoot;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 19/10/12
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public class CodeGeneratorJava extends  AbstractCodeGenerator {

    public CodeGeneratorJava(ContainerRoot model) {
        super(model);
    }

    @Override
    public void execute()
    {
        // gen header ports
        gen_headerPorts.append(getProvidedPort());
        gen_headerPorts.append(getRequiredPort());

        // gen port java to jni port   dispatcher
        gen_ports.append(getJavaDispatcher());

    }


    private String getProvidedPort(){
        StringBuilder gen = new StringBuilder();
        int count=0;
        if(inputs_ports.size() >0 )
        {
            gen.append("@Provides({");
            for (String name : inputs_ports.keySet()){
                gen.append(" @ProvidedPort(name = \""+name+"\", type = PortType.MESSAGE,theadStrategy = ThreadStrategy.NONE)");
                if(count < inputs_ports.size()-1) {
                    gen.append(",\n");
                } else {
                    gen.append("\n");
                }

                count++;
            }
            gen.append("})");
        }

        return gen.toString();
    }

    public String getRequiredPort(){
        StringBuilder gen = new StringBuilder();
        int count=0;

        if(ouputs_ports.size() >0){
            gen.append("@Requires({");
            for (String name : ouputs_ports.keySet()){
                gen.append(" @RequiredPort(name = \""+name+"\", type = PortType.MESSAGE,optional = true,theadStrategy = ThreadStrategy.NONE)");
                if(count < ouputs_ports.size()-1) {
                    gen.append(",\n");
                } else {
                    gen.append("\n");
                }

                count++;
            }
            gen.append("})");
        }
        return gen.toString();
    }

    public String getJavaDispatcher(){

        StringBuilder gen = new StringBuilder();

        for (String name : inputs_ports.keySet())
        {
            gen.append( "    @Port(name = \""+name+"\")\n" +
                    "    public void "+name+"(Object o)\n" +
                    "    {\n" +
                    "        if(nativeManager != null)\n" +
                    "        {\n" +
                    "            nativeManager.push(\""+name+"\",o.toString());\n" +
                    "            \n" +
                    "        }   else \n" +
                    "        {\n" +
                    "            System.err.println(\"Error processing message\");\n" +
                    "        }\n" +
                    "    }");
        }
        return  gen.toString();
    }
}

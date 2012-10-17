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

import org.kevoree.tools.nativeN.api.INativeGen;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 27/09/12
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public class NativeGen implements INativeGen {

    private LinkedHashMap<String,Integer> inputs_ports = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String,Integer> ouputs_ports = new LinkedHashMap<String, Integer>();
    private String pathfile = "";

    public String getPathfile() {
        return pathfile;
    }

    public void setPathfile(String pathfile) {
        this.pathfile = pathfile;
    }

    public LinkedHashMap<String, Integer> getInputs_ports() {
        return inputs_ports;
    }

    public LinkedHashMap<String, Integer> getOuputs_ports() {
        return ouputs_ports;
    }


    public String generateStepPreCompile(){
        StringBuilder gen = new StringBuilder();

        gen.append("#include \"$NAME$.h\"\n\n");
        gen.append(generateOutputsPorts());

        gen.append(generateInputsPorts());

        gen.append(generateMethods());

        return gen.toString();
    }

    public String generateInputsPorts()
    {
        StringBuilder gen = new StringBuilder();
        for (String name : inputs_ports.keySet()){
            gen.append("\n/* @Port(name = \""+name+"\") */\n");
            gen.append("void "+name+"(void *input) {\n");
            gen.append("// USE INPUT\n");
            gen.append("}\n");
        }
        return gen.toString();
    }

    public String generateMethods(){
        StringBuilder gen =new StringBuilder();

        gen.append("\n" +
                "/*@Start*/\n" +
                "int start()\n" +
                "{\n" +
                "\tfprintf(stderr,\"Component starting \\n\");\n" +
                "\n" +
                "return 0;\n" +
                "}\n" +
                "\n" +
                "/*@Stop */\n" +
                "int stop()\n" +
                "{\n" +
                "    fprintf(stderr,\"Component stoping \\n\");\n" +
                "return 0;\n" +
                "}\n" +
                "\n" +
                "/*@Update */\n" +
                "int update()\n" +
                "{\n" +
                "    fprintf(stderr,\"Component updating \\n\");\n" +
                " return 0;\n" +
                "}\n");

        return gen.toString();
    }

    public String generateOutputsPorts(){

        StringBuilder gen = new StringBuilder();
        for (String name : ouputs_ports.keySet()){

            gen.append("extern void "+name+"(void *input);\n");
        }

        return gen.toString();
    }


    public String generateStepCompile(){
        StringBuilder gen = new StringBuilder();


        gen.append("#include \"thirdparty/component.h\" \n\n\n");

        for (String name : inputs_ports.keySet())
        {
            gen.append("void "+name+"(void *input);\n");
        }


        for (String name : ouputs_ports.keySet()){
            gen.append("void "+name+"(void *input) {\n");
            gen.append(" process_output("+ouputs_ports.get(name)+",input);\n");
            gen.append("}\n");

        }
        gen.append("void dispatch(int port,int id_queue)\n" +
                "{\n" +
                "    kmessage *msg = NULL;\n" +
                "    do\n" +
                "    {\n" +
                "          msg = dequeue(id_queue);\n" +
                "          if(msg !=NULL)\n" +
                "          {\n" +
                "             switch(port)\n" +
                "             {");

        for (String name : inputs_ports.keySet()){
            gen.append("\t\t\t case "+inputs_ports.get(name)+":\n");
            gen.append("\t\t\t\t\t "+name+"(msg->value);\n");
            gen.append("\t\t\t break;\n");
        }

        gen.append("                     }\n" +
                "                     }\n" +
                "\n" +
                "    } while(msg != NULL);\n" +
                "}");

        gen.append("int main (int argc,char *argv[])\n" +
                "{\n" +
                "   \tif(argc  > 1)\n" +
                "    {\n" +
                "\t    key_t key =   atoi(argv[1]);\n" +
                "\t   // int port=   atoi(argv[2]);\n" +
                "\n" +
                "\t     bootstrap(key,-1);\n" +
                "        ctx->start= &start;\n" +
                "        ctx->stop = &stop;\n" +
                "        ctx->update   = &update;\n" +
                "        ctx->dispatch = &dispatch;\n" +
                "\t    ctx->start();\n  " +
                "  pause();\n" +
                "     }\n" +
                "}");

        return gen.toString();
    }


    public int create_input(String name)
    {
        inputs_ports.put(name,inputs_ports.size());
        return inputs_ports.size();
    }

    public int create_output(String name)
    {
        ouputs_ports.put(name,ouputs_ports.size());
        return ouputs_ports.size();
    }


    public String gen_bridge_ProvidedPort(){
        StringBuilder gen = new StringBuilder();
        int count=0;

        if(inputs_ports.size() >0 ){

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


    public String gen_bridge_RequiredPort(){
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

    public String gen_bridge_Ports(){

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

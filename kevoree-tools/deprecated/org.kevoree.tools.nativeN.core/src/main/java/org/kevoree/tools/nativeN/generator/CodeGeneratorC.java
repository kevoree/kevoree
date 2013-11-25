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
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class CodeGeneratorC extends  AbstractCodeGenerator {


    public CodeGeneratorC(ContainerRoot model) {
        super(model);
    }


    @Override
    public void execute() {

        gen_headerPorts.append("#include \"$NAME$.h\"\n\n");
        gen_headerPorts.append(generateOutputsPorts());
        gen_headerPorts.append(generateInputsPorts());
        gen_headerPorts.append(generateMethods());

        gen_body.append(generateBodyC());
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


    public String generateBodyC()
    {
        StringBuilder gen = new StringBuilder();

        gen.append("#include <dlfcn.h>\n");
        gen.append("#include \"thirdparty/component.h\" \n\n\n");

        for (String name : inputs_ports.keySet())
        {
            gen.append("void "+name+"(void *input);\n");
        }

        gen.append("void dummy_function() { }\n" +
                "static const char *get_runtime_path ()\n" +
                "{\n" +
                "    Dl_info info;\n" +
                "    if (0 == dladdr((void*)dummy_function, &info)) return \"unknown\";\n" +
                "    return info.dli_fname;\n" +
                "}\n");

        gen.append("const char * getRessource(const char*key)\n" +
                "{\n" +
                "   int length=0;\n" +
                "   char path_ressource[2048];\n" +
                "   const char *path_uexe = get_runtime_path();\n" +
                "   length = strlen(rindex(path_uexe, '/'));\n" +
                "   memset(path_ressource,0,sizeof(path_ressource));\n" +
                "   strncpy(path_ressource,get_runtime_path(),strlen(get_runtime_path()) - length);\n" +
                "   strcat(path_ressource,\"/"+getComponentType().getName()+"/\");\n" +
                "   strcat(path_ressource,key);\n" +
                "  return strdup(path_ressource);\n" +
                "}");

        for (String name : ouputs_ports.keySet()){
            gen.append("void "+name+"(void *input) {\n");
            gen.append(" process_output("+ouputs_ports.get(name)+",input);\n");
            gen.append("}\n");

        }
        gen.append("void dispatch(int port,int id_queue)\n" +
                "{\n" +
                "    kmessage *msg = NULL;\n" +
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
                "        // todo fix here we are waiting that dico are sets\n" +
                "       sleep(2); \n" +
                "\t    ctx->start();\n " +
                "  pause();\n" +
                "     }\n" +
                "}");

        return gen.toString();
    }

}

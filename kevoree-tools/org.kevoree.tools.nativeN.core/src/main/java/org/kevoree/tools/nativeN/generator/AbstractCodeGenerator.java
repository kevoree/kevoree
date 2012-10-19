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

import org.kevoree.*;
import org.kevoree.tools.nativeN.api.ICodeGenerator;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 19/10/12
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public abstract  class AbstractCodeGenerator implements ICodeGenerator
{
    protected ContainerRoot model= null;

    // code generated
    protected StringBuilder gen_headerPorts = new StringBuilder();
    protected StringBuilder gen_ports = new StringBuilder();
    protected StringBuilder gen_body = new StringBuilder();


    protected LinkedHashMap<String,Integer> inputs_ports = new LinkedHashMap<String, Integer>();
    protected LinkedHashMap<String,Integer> ouputs_ports = new LinkedHashMap<String, Integer>();
    protected LinkedHashMap<String,String> dicos = new LinkedHashMap<String, String>();

    public  AbstractCodeGenerator(ContainerRoot model)
    {
        this.model = model;
        for(TypeDefinition type :  model.getTypeDefinitionsForJ()) {
            if(type instanceof ComponentType) {
                ComponentType c = (ComponentType)type;
                for(PortTypeRef portP :  c.getProvidedForJ() )    {  create_input(portP.getName()); }
                for(PortTypeRef portR :  c.getRequiredForJ()) { create_output(portR.getName()); }
                if( c.getDictionaryType().isDefined())
                {
                    for(DictionaryAttribute entry:  c.getDictionaryType().get().getAttributesForJ()){
                        dicos.put(entry.getName(),"default");
                    }
                }
            }
        }
    }

    private int create_input(String name)
    {
        inputs_ports.put(name,inputs_ports.size());
        return inputs_ports.size();
    }

    private int create_output(String name)
    {
        ouputs_ports.put(name,ouputs_ports.size());
        return ouputs_ports.size();
    }

    @Override
    public String getHeaderPorts() {
        return gen_headerPorts.toString();
    }

    @Override
    public String getPorts() {
        return gen_ports.toString();
    }


    @Override
    public String getBody() {
        return gen_body.toString();
    }



}

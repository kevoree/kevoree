package org.kevoree.library.javase.webserver.reasoner;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.api.service.core.script.ScriptInterpreter;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 28/11/11
 * Time: 19:31
 * To change this template use File | Settings | File Templates.
 */


@ComponentType
public class LatexReasoner extends AbstractPage {

    
    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        if (request.getUrl().endsWith("login")) {

            //DO CLEVER STUFF ;-)
            String userLogin = request.getResolvedParams().get("login");
            String userPassword = request.getResolvedParams().get("pass");
            String userUrl = request.getResolvedParams().get("pass");


            if (userLogin != null && userPassword != null && userUrl != null) {

                //
                StringBuilder script = new StringBuilder();
                script.append("tblock{ \n");
                //CREATE ON SELF NODE ; TODO BETTER :-)
                //ADD FILE SYSTEM DEDICATED TO USER
                script.append("addComponent SvnFileSys_" + userLogin + "@" + this.getNodeName() + " : SvnFileSystem");
                    script.append("{");
                    script.append("url="+userUrl+",");
                    script.append("login="+userLogin+",");
                    script.append("pass="+userPassword);
                    script.append("}\n");

                script.append("addComponent Editor_"+userLogin + "@" + this.getNodeName() + " : LatexEditor { urlpattern=/latexeditor/"+userLogin+"/** }");
                script.append("addComponent TexCompiler_"+userLogin + "@" + this.getNodeName() + " : LatexCompiler ");

                //GENERATE BINDING FOR FILES SERVICES
                script.append("addChannel filesService_"+userLogin+" : defSERVICE");
                script.append("bind Editor_"+userLogin+".files@"+this.getNodeName()+" => filesService_"+userLogin);
                script.append("bind SvnFileSys_"+userLogin+".files@"+this.getNodeName()+" => filesService_"+userLogin);

                //GENERATE BINDING FOR COMPILER SERVICES
                script.append("addChannel TexCompiler_in_"+userLogin+" : defMSG");
                script.append("addChannel TexCompiler_out_"+userLogin+" : defMSG");
                script.append("bind Editor_"+userLogin+".compile@"+this.getNodeName()+" => TexCompiler_in_"+userLogin);
                script.append("bind TexCompiler_"+userLogin+".COMPILE@"+this.getNodeName()+" => TexCompiler_in_"+userLogin);
                script.append("bind TexCompiler_"+userLogin+".COMPILE_CALLBACK@"+this.getNodeName()+" => TexCompiler_out_"+userLogin);
                script.append("bind SvnFileSys_"+userLogin+".compileCallback@"+this.getNodeName()+" => TexCompiler_out_"+userLogin);

                //BIND TO CURRENT WEB SERVER
                ContainerRoot model = this.getModelService().getLastModel();
                ContainerNode selfNode = null;
                for(ContainerNode loopNode : model.getNodesForJ()){
                    if(loopNode.getName().equals(this.getNodeName())){
                        selfNode = loopNode;
                    }
                }
                ComponentInstance webserver = null;
                for(ComponentInstance loopComponent :  selfNode.getComponentsForJ()){
                    if(loopComponent.getTypeDefinition().getName().equals("WebServer")){
                        webserver = loopComponent;
                    }
                }

                script.append("addChannel KloudTextRequest : defMSG");
                script.append("bind "+webserver.getName()+".handler@"+this.getNodeName()+" => KloudTextRequest");
                script.append("bind Editor_"+userLogin+".request@"+this.getNodeName()+" => KloudTextRequest");

                script.append("addChannel KloudTextResponse : defMSG");
                script.append("bind "+webserver.getName()+".response@"+this.getNodeName()+" => KloudTextResponse");
                script.append("bind Editor_"+userLogin+".content@"+this.getNodeName()+" => KloudTextResponse");


                script.append("}//end tblock \n");
                
                Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
                ServiceReference ref = bundle.getBundleContext().getServiceReference(ScriptInterpreter.class.getName());
                ScriptInterpreter kevs = (ScriptInterpreter) bundle.getBundleContext().getService(ref);
                if(kevs.interpret(script.toString())){
                    response.setContent("<html><body><a href=\"/latexeditor/"+userLogin+"\">Go to your dedicated editor "+userLogin+"</a></body></html>");
                    response.setContentType("text/html");
                } else {
                    response.setContent("Error while updating server");
                }




                return response;
            }

            //TODO
        } else {
            if (FileServiceHelper.checkStaticFile("index.html", this, request, response)) {
                return response;
            }
            response.setContent("Bad request");
        }

        return response;
    }

}

package org.kevoree.library.javase.webserver.reasoner;

import org.kevoree.*;
import org.kevoree.annotation.ComponentType;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 28/11/11
 * Time: 19:31
 */


@ComponentType
public class LatexReasoner extends AbstractPage {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        if (request.getUrl().endsWith("login")) {

            //DO CLEVER STUFF ;-)
            String userLogin = request.getResolvedParams().get("login");
            String userPassword = request.getResolvedParams().get("pass");
            String userUrl = request.getResolvedParams().get("url");


            if (userLogin != null && userPassword != null && userUrl != null) {

                //
                StringBuilder script = new StringBuilder();
                script.append("tblock{ \n");
                //CREATE ON SELF NODE ; TODO BETTER :-)
                //ADD FILE SYSTEM DEDICATED TO USER
                script.append("addComponent SvnFileSys_" + userLogin + "@" + this.getNodeName() + " : SvnFileSystem");
                    script.append("{");
                    script.append("url=\""+userUrl+"\",");
                    script.append("login=\""+userLogin+"\",");
                    script.append("pass=\""+userPassword+"\"");
                    script.append("}\n");

                script.append("addComponent Editor_"+userLogin + "@" + this.getNodeName() + " : LatexEditor { urlpattern=\"/latexeditor/"+userLogin+"/**\" }\n");
                script.append("addComponent TexCompiler_"+userLogin + "@" + this.getNodeName() + " : LatexCompiler\n");

                //GENERATE BINDING FOR FILES SERVICES
                script.append("addChannel filesService_"+userLogin+" : defSERVICE\n");
                script.append("bind Editor_"+userLogin+".files@"+this.getNodeName()+" => filesService_"+userLogin+"\n");
                script.append("bind SvnFileSys_"+userLogin+".files@"+this.getNodeName()+" => filesService_"+userLogin+"\n");

                //GENERATE BINDING FOR COMPILER SERVICES
                script.append("addChannel TexCompiler_in_"+userLogin+" : defMSG\n");
                script.append("addChannel TexCompiler_out_"+userLogin+" : defMSG\n");
                script.append("bind Editor_"+userLogin+".compile@"+this.getNodeName()+" => TexCompiler_in_"+userLogin+"\n");
                script.append("bind TexCompiler_"+userLogin+".COMPILE@"+this.getNodeName()+" => TexCompiler_in_"+userLogin+"\n");
                script.append("bind TexCompiler_"+userLogin+".COMPILE_CALLBACK@"+this.getNodeName()+" => TexCompiler_out_"+userLogin+"\n");
                script.append("bind Editor_"+userLogin+".compileCallback@"+this.getNodeName()+" => TexCompiler_out_"+userLogin+"\n");

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
                Port requirePort = null;
                for(Port loopPort : webserver.getRequiredForJ()){
                    if(loopPort.getPortTypeRef().getName().equals("handler")){
                        requirePort = loopPort;
                    }
                }
                Channel requestChannel = null; 
                for(MBinding mb : model.getMBindingsForJ()){
                    if(mb.getPort().equals(requirePort)){
                        requestChannel = mb.getHub();
                    }
                }

                script.append("bind Editor_"+userLogin+".request@"+this.getNodeName()+" => "+requestChannel.getName()+"\n");
                script.append("addChannel KloudTextResponse : defMSG\n");
                script.append("bind "+webserver.getName()+".response@"+this.getNodeName()+" => KloudTextResponse\n");
                script.append("bind Editor_"+userLogin+".content@"+this.getNodeName()+" => KloudTextResponse\n");


                script.append("}//end tblock \n");

                logger.debug("Script result \n"+script.toString());
                
               // Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
               // ServiceReference ref = bundle.getBundleContext().getServiceReference(ScriptInterpreter.class.getName());
               // ScriptInterpreter kevs = (ScriptInterpreter) bundle.getBundleContext().getService(ref);

                KevScriptEngine engine = this.getKevScriptEngineFactory().createKevScriptEngine();
                engine.append(script.toString());

                if(engine.atomicInterpretDeploy()){
                    response.setContent("<html><body><a href=\"/latexeditor/"+userLogin+"/\">Go to your dedicated editor "+userLogin+"</a></body></html>");
//                    response.setContentType("text/html");
					response.getHeaders().put("Content-Type", "text/html");
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

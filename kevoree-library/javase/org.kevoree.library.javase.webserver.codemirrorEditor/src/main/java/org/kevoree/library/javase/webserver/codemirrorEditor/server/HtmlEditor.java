package org.kevoree.library.javase.webserver.codemirrorEditor.server;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.kevoree.library.javase.webserver.servlet.LocalServletRegistry;


@ComponentType 
/*@Requires({
        @RequiredPort(name = "markdown2html", type = org.kevoree.annotation.PortType.SERVICE, className = org.kevoree.library.javase.markdown2html.MarkDown2HtmlService.class)
})*/
public class HtmlEditor extends ParentAbstractPage {


    private LocalServletRegistry servletRepository = null;

    
    public void startPage() {
        servletRepository = new LocalServletRegistry(){
            @Override
            public String getCDefaultPath(){
                return "/htmleditor";
            }
        };
        super.startPage();
//        servletRepository.registerServlet("/markdown2htmlwar/markdown2html",new MarkDown2HtmlServiceImpl(this));
        //servletRepository.unregisterUrl(("/markdown2html/markdown2htmlservice");
    }

    
    
    
    @Override
    public KevoreeHttpResponse process(final KevoreeHttpRequest request, final KevoreeHttpResponse response) {

    	
/*    	ClassLoader l = Thread.currentThread().getContextClassLoader();
    	Thread.currentThread().setContextClassLoader(MarkDown2Html.class.getClassLoader() );
    	boolean res = servletRepository.tryURL(request.getUrl(),request,response);
    	Thread.currentThread().setContextClassLoader(l );
    	if ( res ){	
    		return response;
    		
    	}*/        
    	
    	
        if (FileServiceHelper.checkStaticFile(request.getUrl(), this, request, response)) {
            return response;
        }
        if (FileServiceHelper.checkStaticFile("codemirrorEditor.html", this, request, response)) {
            return response;
        }
        response.setContent("Bad request1");
        return response;
    }
    

}
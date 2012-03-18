package org.kevoree.library.javase.webserver.markdown2htmlwar.server;

import org.kevoree.annotation.ComponentType;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.markdown2htmlwar.client.MarkDown2HtmlService;
import org.kevoree.library.javase.webserver.servlet.LocalServletRegistry;

import com.google.gwt.user.server.rpc.RPC;


@ComponentType 
/*@Requires({
        @RequiredPort(name = "files", type = org.kevoree.annotation.PortType.SERVICE, className = MarkDown2HtmlService.class)
})*/
class MyTask extends Thread{

	private LocalServletRegistry servletRepository = null;
	KevoreeHttpRequest request;
	KevoreeHttpResponse response;
	
	boolean result=false;
	
	public boolean isResult() {
		return result;
	}



	public MyTask(LocalServletRegistry servletRepository,
			KevoreeHttpRequest request, KevoreeHttpResponse response) {
		super();
		this.servletRepository = servletRepository;
		this.request = request;
		this.response = response;
	}

	
	
	@Override
	public void run() {
		Thread.currentThread().setContextClassLoader(MarkDown2HtmlService.class.getClassLoader() );
        result = servletRepository.tryURL(request.getUrl(),request,response);
        }

	
}


public class MarkDown2Html extends AbstractPage {


    private LocalServletRegistry servletRepository = null;

    public void startPage() {
        servletRepository = new LocalServletRegistry(){
            @Override
            public String getCDefaultPath(){
                return "/markdown2htmlwar";
            }
        };
        super.startPage();
        ((KevoreeJarClassLoader)RPC.class.getClassLoader()).addWeakClassLoader(Thread.currentThread().getContextClassLoader());
        servletRepository.registerServlet("/markdown2htmlwar/markdown2html",new MarkDown2HtmlServiceImpl(this));
        //servletRepository.unregisterUrl(("/markdown2html/markdown2htmlservice");
    }

 
   
    
    @Override
    public KevoreeHttpResponse process(final KevoreeHttpRequest request, final KevoreeHttpResponse response) {

    	
    	MyTask t = new MyTask(servletRepository, request, response);
    	t.start();
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	if (t.isResult())
    		return response;
        if (FileServiceHelper.checkStaticFile(request.getUrl(), this, request, response)) {
            return response;
        }
        if (FileServiceHelper.checkStaticFile("Markdown2htmlwar.html", this, request, response)) {
            return response;
        }
        response.setContent("Bad request1");
        return response;
    }
    
    
    
	public String markdown2html(String name) {
		return "toto";
	}


}
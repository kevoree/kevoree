package org.kevoree.library.javase.markdown2html;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Port;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

import com.petebevin.markdown.MarkdownProcessor;



@Provides({@ProvidedPort(name="markdown2html",type= PortType.SERVICE,className=MarkDown2HtmlService.class)
})
@ComponentType
public class MarkDown2HtmlComponent extends AbstractComponentType implements MarkDown2HtmlService {


	MarkdownProcessor p = new MarkdownProcessor();
	
    @Start
    public void startComponent() {
    }

    @Stop
    public void stopComponent() {
    }

    @Port(name="markdown2html",method="markdown2html")
	public String markdown2html(String name) {
		return p.markdown(name);
	}
}

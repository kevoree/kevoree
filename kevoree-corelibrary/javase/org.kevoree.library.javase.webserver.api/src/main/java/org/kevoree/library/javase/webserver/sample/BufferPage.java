package org.kevoree.library.javase.webserver.sample;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/11/11
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@ComponentType
public class BufferPage extends AbstractPage {

    private ArrayList<String> buffers = new ArrayList<String>();

    @Port(name = "input")
    public void onMessage(Object o) {
        buffers.add(0,o.toString());
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>\n" +
                "<head>\n" +
                "<script type=\"text/JavaScript\">\n" +
                "<!--\n" +
                "function timedRefresh(timeoutPeriod) {\n" +
                "\tsetTimeout(\"location.reload(true);\",timeoutPeriod);\n" +
                "}\n" +
                "//   -->\n" +
                "</script>\n" +
                "</head>\n" +
                "<body onload=\"JavaScript:timedRefresh(600);\">\n" );

        for (String in : buffers) {
            buffer.append(in + "<br />");
        }
        buffer.append("</body></html>");
        response.setContent(buffer.toString());
        return response;
    }

}

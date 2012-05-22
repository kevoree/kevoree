package org.kevoree.library.webserver.sample.monitoring;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/05/12
 * Time: 15:33
 */

@ComponentType
public class MonitorPage extends AbstractPage {

    private final Meter requests = Metrics.newMeter(MonitorPage.class, this.getClass().getName()+"_"+getName(), "requests", TimeUnit.SECONDS);

    @Override
    public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
        requests.mark();
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        builder.append("This is a sample monitored page <br />");
        builder.append("Hello from Kevoree from url " + request.getUrl() + " <br />");
        for (String key : request.getResolvedParams().keySet()) {
            builder.append(key + "->" + request.getResolvedParams().get(key) + "<br>");
        }
        builder.append("lastParam->" + getLastParam(request.getUrl()) + "<br>");
        builder.append("Served by node "+this.getNodeName()+"<br />");
        builder.append("</body></html>");
        response.setContent(builder.toString());
        return response;
    }
}

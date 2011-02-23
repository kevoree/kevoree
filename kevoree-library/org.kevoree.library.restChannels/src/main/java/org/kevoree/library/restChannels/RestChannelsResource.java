package org.kevoree.library.restChannels;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class RestChannelsResource extends ServerResource {

    protected void doInit() throws ResourceException {
        setExisting(true);
    }

    public Representation doHandle() {
        if (getMethod().equals(Method.GET)) {
            StringRepresentation result = new StringRepresentation(getHTMLList());
            result.setMediaType(MediaType.TEXT_HTML);
            return result;
        }
        return new StringRepresentation("only GET method allowed");
    }

    private String getHTMLList() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");
        buffer.append("<body>");
        buffer.append("<div>");
        buffer.append("channels link:");
        buffer.append("<br />");

        for (String key : RestChannelFragmentResource.channels.keySet()) {
            buffer.append("<a href=\"/channels/");
            buffer.append(key);
            buffer.append("\">");
            buffer.append(key);
            buffer.append("</a><br />");
        }

        buffer.append("</div>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }
}

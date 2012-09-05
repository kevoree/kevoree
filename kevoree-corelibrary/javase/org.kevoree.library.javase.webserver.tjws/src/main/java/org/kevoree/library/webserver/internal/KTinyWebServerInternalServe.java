package org.kevoree.library.webserver.internal;

import Acme.Serve.SelectorAcceptor;
import Acme.Serve.SimpleAcceptor;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/12
 * Time: 16:45
 */
public class KTinyWebServerInternalServe extends Acme.Serve.Serve  {

    public void setMappingTable(PathTreeDictionary mappingtable) {
        super.setMappingTable(mappingtable);
    }

    @Override
    protected Acceptor createAcceptor() throws IOException {
		Acceptor acceptor = new SimpleAcceptor();
        Map acceptorProperties = new Properties();
        acceptor.init(arguments, acceptorProperties);
        hostName = (String) acceptorProperties.get(ARG_BINDADDRESS);
        return acceptor;
    }
}

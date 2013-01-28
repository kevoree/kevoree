package org.kevoree.library.webserver.sample.monitoring;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/09/12
 * Time: 19:59
 */
@ComponentType
public class CurrentModelReport extends AbstractPage implements ModelListener {

    private AtomicReference<String> pageBuffer = new AtomicReference<String>();

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        response.setContent(pageBuffer.get());
        return response;
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public void modelUpdated() {
        ContainerRoot currentModel = getModelService().getLastModel();
        Document doc = Jsoup.parse(KevoreeXmiHelper.$instance.saveToString(currentModel, true), "ContainerRoot", Parser.xmlParser());
        Elements elems = doc.select("ContainerRoot > ContainerNode");
        System.out.println("Node:" + elems.size());
        for (Element e : elems) {
            Elements elems2 = doc.select("ContainerNode[name=" + e.getElementById("name").text() + "]");
            System.out.println("Node:" + elems2.size());
        }
    }

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
    public void startPage() {
        super.startPage();
        getModelService().registerModelListener(this);
    }

    @Override
    public void stopPage() {
        getModelService().unregisterModelListener(this);
        super.stopPage();
    }

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.parse(new FileInputStream("/Users/duke/Desktop/baliseHM.kev"), "UTF-8", "kevoree:containerroot", Parser.xmlParser());
        Elements elems = doc.select("nodes");
        System.out.println("Node:" + elems.size());
        for (Element e : elems) {
            System.out.println(e.attributes().get("name"));
            Elements subElems = doc.select("nodes[name=" + e.attributes().get("name") + "] components");
            System.out.println("Sub:" + subElems.size());

        }

    }
}

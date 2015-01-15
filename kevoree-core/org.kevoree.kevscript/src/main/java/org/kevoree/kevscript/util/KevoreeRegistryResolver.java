package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.util.ModelVisitor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duke on 9/15/14.
 */
public class KevoreeRegistryResolver {

    public static void main(String[] args) throws Exception {
        ArrayList<TypeFQN> fqn = new ArrayList<TypeFQN>();
        TypeFQN fq = new TypeFQN();
        fq.name = "JavaNode";
        fqn.add(fq);
        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot root = factory.createContainerRoot();
        factory.root(root);
        resolve(fqn, root, factory);

        factory.createJSONSerializer().serializeToStream(root, System.out);
    }

    public static boolean resolve(List<TypeFQN> fqns, ContainerRoot current, KevoreeFactory factory) throws Exception {

        StringBuilder messageBuffer = new StringBuilder();
        for(TypeFQN fqn : fqns){
            messageBuffer.append(fqn.name);
            if(fqn.version != null){
                messageBuffer.append(":");
                messageBuffer.append(fqn.version);
            }
            messageBuffer.append(",");
        }
        Log.info("Request="+messageBuffer.toString());

        String kevoreeRegistry = System.getProperty("kevoree.registry");
        String kevoreeVersion = new DefaultKevoreeFactory().getVersion();
        if (kevoreeVersion.contains(".")) {
            kevoreeVersion = kevoreeVersion.substring(0, kevoreeVersion.indexOf("."));
        }
        if (kevoreeRegistry == null) {
            kevoreeRegistry = "http://registry.kevoree.org/v" + kevoreeVersion + "/";
            //kevoreeRegistry = "http://localhost:8080/";
        }
        StringBuilder request = new StringBuilder();
        request.append("[");
        boolean isFirst = true;
        for (TypeFQN fq : fqns) {
            String typePath = convertFQN2Path(fq);
            if (current.select(typePath).isEmpty()) {
                if (!isFirst) {
                    request.append(",");
                }
                request.append("\"");
                request.append(convertFQN2Path(fq));
                request.append("\"");
                isFirst = false;
            }
        }
        request.append("]");

        try {
            String model = collectModel(kevoreeRegistry, request.toString());
            if (model != null && !model.isEmpty()) {
                ContainerRoot modelRoot = (ContainerRoot) factory.createJSONLoader().loadModelFromString(model).get(0);
                final Integer[] i = {0};
                final StringBuilder builder = new StringBuilder();
                modelRoot.deepVisitReferences(new ModelVisitor() {
                    @Override
                    public void visit(KMFContainer kmfContainer, String s, KMFContainer kmfContainer2) {
                        if (kmfContainer instanceof TypeDefinition) {
                            i[0]++;
                            if(builder.length()!=0){
                               builder.append(",");
                            }
                            TypeDefinition td = (TypeDefinition) kmfContainer;
                            builder.append(td.getName());
                            builder.append("/");
                            builder.append(td.getVersion());
                        }
                    }
                });
                Log.info("Resolved {} typeDefinitions from Kevoree Registry {} ({})", i[0], kevoreeRegistry,builder.toString());
                try {
                    factory.createModelCompare().merge(current, modelRoot).applyOn(current);
                } catch (Exception e) {
                    Log.error("Error while merging TypeDefinitions from Registry ! ", e);
                }
            }
        } catch (Exception e) {
            Log.debug("", e);
        }
        return true;
    }

    private static String collectModel(String url, String payload) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "KevoreeClient");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }


    public static String convertFQN2Path(TypeFQN fqn) throws Exception {
        String[] elements = fqn.name.split("\\.");
        if (elements.length <= 1) {
            return "**/typeDefinitions[name=" + fqn.name + "]";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.length - 1; i++) {
            builder.append("/packages[");
            builder.append(URLEncoder.encode(elements[i], "UTF-8"));
            builder.append("]");
        }
        builder.append("/typeDefinitions[name=");
        builder.append(elements[elements.length - 1]);
        if (fqn.version != null) {
            builder.append(",version=" + URLEncoder.encode(fqn.version, "UTF-8"));
        }
        builder.append("]");
        return builder.toString();
    }


}

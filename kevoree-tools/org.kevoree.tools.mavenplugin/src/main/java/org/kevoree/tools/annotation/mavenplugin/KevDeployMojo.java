package org.kevoree.tools.annotation.mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by duke on 8/27/14.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, requiresDependencyResolution = ResolutionScope.COMPILE)
public class KevDeployMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/classes/KEV-INF/lib.json")
    private File outputLibrary;


    public static String getMajorVersion() {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        String kevoreeVersion = factory.getVersion();
        if (kevoreeVersion.contains(".")) {
            kevoreeVersion = kevoreeVersion.substring(0, kevoreeVersion.indexOf("."));
        }
        return kevoreeVersion;
    }

    @Parameter(defaultValue = "http://registry.kevoree.org/")
    private String registry;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (registry.equals("http://registry.kevoree.org/")) {
            registry = registry +"v"+ getMajorVersion() + "/";
        }

        if (outputLibrary != null && outputLibrary.exists()) {
            try {
                FileInputStream fis = new FileInputStream(outputLibrary);
                String payload = getStringFromInputStream(fis);
                send(payload);
                fis.close();
            } catch (Exception e) {
                throw new MojoExecutionException("Bad deployment of Kevoree library to index ", e);
            }
        }
    }

    private String send(String payload) throws Exception {
        if (!registry.endsWith("/")) {
            registry = registry + "/";
        }
        URL obj = new URL(registry + "deploy");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "KevoreeClient");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type","application/json");
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

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}

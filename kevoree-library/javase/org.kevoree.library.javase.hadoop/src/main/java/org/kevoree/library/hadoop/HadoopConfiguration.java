/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import java.util.Scanner;

/**
 *
 * @author sunye
 */
public class HadoopConfiguration {

    private Configuration configuration;
    private String confDir;

    public HadoopConfiguration(Configuration p) {
        this.configuration = p;
        this.confDir = configuration.get("hadoop.home") + "/conf";
        new File(confDir).mkdirs();
        this.environmentVariables();

        this.logs();

    }

    private void environmentVariables() {
        System.setProperty("hadoop.home", configuration.get("hadoop.home"));
    }

    private void logs() {

        String logDir = configuration.get("hadoop.log.dir");
        new File(logDir).mkdirs();
        new File(logDir + "history/").mkdirs();

        System.setProperty("hadoop.log.dir", logDir);
        System.setProperty("hadoop.root.logger", "DEBUG,console");
        System.setProperty("hadoop.log.file", "hadoop.log");
    }

    public void writeMapredSite() throws IOException {
        String fileName = "/mapred-site.xml";
        FileWriter f = new FileWriter(confDir + fileName);
        BufferedWriter bw = new BufferedWriter(f);

        bw.write("<configuration>\n");
        bw.write("<property>\n");
        bw.write("<name>mapred.job.tracker</name>\n");
        bw.write("<value>");
        bw.write(configuration.get("hadoop.jobtracker"));
        bw.write(":");
        bw.write(configuration.get("hadoop.jobtracker.port"));
        bw.write("</value>\n");
        bw.write("</property>\n");
        bw.write("</configuration>\n");
        bw.close();

    }

    public void writeCoreSite() throws IOException {
        String fileName = "/core-site.xml";
        FileWriter f = new FileWriter(confDir + fileName);
        BufferedWriter bw = new BufferedWriter(f);

        bw.write("<configuration>\n");
        bw.write("<property>\n");
        bw.write("<name>fs.default.name</name>\n");
        bw.write("<value>hdfs://");
        bw.write(configuration.get("hadoop.namenode"));
        bw.write(":");
        bw.write(configuration.get("hadoop.namenode.port"));
        bw.write("</value>\n");
        bw.write("</property>\n");
        bw.write("</configuration>\n");
        bw.close();
    }

    public void writeHdfsSite() throws IOException {
        String fileName = "/hdfs-site.xml";
        FileWriter f = new FileWriter(confDir + fileName);
        BufferedWriter bw = new BufferedWriter(f);

        bw.write("<configuration>\n");

        bw.write("<property>\n");
        bw.write("<name>dfs.name.dir</name>\n");
        bw.write("<value>");
        bw.write(configuration.get("dfs.name.dir"));
        bw.write("</value>\n");
        bw.write("</property>\n");

        bw.write("<property>\n");
        bw.write("<name>dfs.data.dir</name>\n");
        bw.write("<value>");
        bw.write(configuration.get("dfs.data.dir"));
        bw.write("</value>\n");
        bw.write("</property>\n");

        bw.write("</configuration>\n");
        bw.close();
    }

    public void writeLog4J() throws IOException {
        String fileName = "/log4j.properties";
        FileWriter fw = new FileWriter(confDir + fileName);
        BufferedWriter bw = new BufferedWriter(fw);

        InputStream is = this.getClass().getResourceAsStream("/log4j.properties");
        Scanner sc = new Scanner(new InputStreamReader(is));

        while (sc.hasNextLine()) {
            bw.write(sc.nextLine());
            bw.newLine();
        }

        bw.close();
    }

    static public boolean removeDir(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    removeDir(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static int findFreePort()
            throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

    public static void main(String[] args) {
        try {
            Configuration p = new Configuration();
            p.set("hadoop.home", "/Users/sunye/Work/hadoop");
            p.set("hadoop.jobtracker", "localhost");
            p.set("hadoop.jobtracker.port", "50");
            p.set("hadoop.namenode", "localhost");
            p.set("hadoop.namenode.port", "9000");
            p.set("dfs.name.dir", "namedir");
            p.set("dfs.data.dir", "datadir");

            HadoopConfiguration cfg = new HadoopConfiguration(p);
            cfg.writeLog4J();

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }

    }
}

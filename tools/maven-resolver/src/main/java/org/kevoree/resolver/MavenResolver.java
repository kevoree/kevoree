package org.kevoree.resolver;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.kevoree.log.Log;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

/**
 *
 * Created by leiko on 2/27/17.
 */
public class MavenResolver {

    private static final Map<String, URL> EMPTY_MAP = new HashMap<>();
    private static File propFile = Paths.get(System.getProperty("user.home"), ".kevoree", "java-logging.properties").toFile();
    private static boolean offline = false;

    static {
        // be sure that the logging of the Maven resolver won't be too verbose
        // but let the user modify the behavior if needed
        String loggingProp = System.getProperty("java.util.logging.config.file");
        if (loggingProp == null) {
            if (!propFile.exists()) {
                propFile.getParentFile().mkdirs();
                try {
                    PrintWriter writer = new PrintWriter(propFile);
                    writer.println("# Specify the handlers to create in the root logger");
                    writer.println("handlers= java.util.logging.ConsoleHandler");
                    writer.println("# Set the default logging level for new ConsoleHandler instances");
                    writer.println("java.util.logging.ConsoleHandler.level= SEVERE");
                    writer.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Unable to write " + propFile.getAbsolutePath(), e);
                }
            }
        } else {
            propFile = new File(loggingProp);
        }

        offline = Boolean.valueOf(System.getProperty("resolver.offline", "false"));
        if (offline) {
            Log.info("Kevoree Maven Resolver is in offline mode");
        }
    }

    public static ConfigurableMavenResolverSystem get() {
        return MavenResolver.get(EMPTY_MAP);
    }

    public static ConfigurableMavenResolverSystem get(Map<String, URL> repositories) {
        ConfigurableMavenResolverSystem resolver = Maven
                .configureResolver()
                .useLegacyLocalRepo(true)
                .workOffline(offline);

        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(propFile));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + propFile.getAbsolutePath());
        }

        for (Map.Entry<String, URL> repo : repositories.entrySet()) {
            resolver.withRemoteRepo(repo.getKey(), repo.getValue(), "default");
        }

        return resolver;
    }
}

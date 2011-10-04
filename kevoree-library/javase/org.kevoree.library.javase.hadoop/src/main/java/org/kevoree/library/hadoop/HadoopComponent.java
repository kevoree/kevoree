/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.kevoree.framework.AbstractComponentType;

/**
 *
 * @author sunye
 */
public class HadoopComponent extends AbstractComponentType {

    private static final String PROPERTIES_FILE = "/hadoop.properties";
    private final Properties properties = new Properties();
    private final Configuration configuration = new Configuration();

    public HadoopComponent() {
        this.loadProperties();
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    private void loadProperties() {
        InputStream is = this.getClass().getResourceAsStream(PROPERTIES_FILE);
        try {
            properties.load(is);
            is.close();
        }
        catch (IOException ex) {
            System.err.println("Could not find resource.");
            System.exit(1);
        }

        for (Object each : properties.keySet()) {
            String key = (String) each;
            configuration.set(key, properties.getProperty(key));
        }
    }
}

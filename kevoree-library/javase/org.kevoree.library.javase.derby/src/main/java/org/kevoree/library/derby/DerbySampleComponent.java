package org.kevoree.library.derby;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Library(name = "Kevoree-Components")
@ComponentType
public class DerbySampleComponent extends AbstractComponentType {

    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private Logger logger = LoggerFactory.getLogger(DerbySampleComponent.class);

    @Start
    public void start() {
        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        try {
            bundle.loadClass(driver).newInstance();
           // DriverManager.registerDriver();
            Connection conn = DriverManager.getConnection("jdbc:derby:" + this.getName()+";create=true", new Properties());
            //TODO SCRIPT CREATION TABLES, IF NOT EXIST

            logger.debug("derby started !");

        } catch (Exception e) {
//            e.printStackTrace();
			logger.error("Fail to start " + this.getName(), e);
        }


    }

    @Stop
    public void stop() {
        try {
            DriverManager.getConnection("jdbc:derby:"+this.getName()+";shutdown=true");
        } catch (SQLException e) {
           // e.printStackTrace();
			logger.warn("Error when we try to stop " + this.getName(), e);
        }
    }

}

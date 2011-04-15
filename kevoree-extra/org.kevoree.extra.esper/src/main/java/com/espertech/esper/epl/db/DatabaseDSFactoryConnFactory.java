package com.espertech.esper.epl.db;

import com.espertech.esper.client.ConfigurationDBRef;
import com.espertech.esper.client.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection factory using {@link javax.naming.InitialContext} and {@link javax.sql.DataSource} to obtain connections.
 */
public class DatabaseDSFactoryConnFactory implements DatabaseConnectionFactory
{
    private static final Log log = LogFactory.getLog(DatabaseDSFactoryConnFactory.class);
    
    private final ConfigurationDBRef.ConnectionSettings connectionSettings;
    private DataSource dataSource;

    /**
     * Ctor.
     * @param dsConfig is the datasource object name and initial context properties.
     * @param connectionSettings are the connection-level settings
     * @throws DatabaseConfigException when the factory cannot be configured
     */
    public DatabaseDSFactoryConnFactory(ConfigurationDBRef.DataSourceFactory dsConfig,
                                 ConfigurationDBRef.ConnectionSettings connectionSettings)
            throws DatabaseConfigException
    {
        this.connectionSettings = connectionSettings;

        Class clazz;
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(dsConfig.getFactoryClassname(), true, cl);
        }
        catch (ClassNotFoundException e)
        {
            throw new DatabaseConfigException("Class '" + dsConfig.getFactoryClassname() + "' cannot be loaded");
        }

        Object obj;
        try
        {
            obj = clazz.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new ConfigurationException("Class '" + clazz + "' cannot be instantiated");
        }
        catch (IllegalAccessException e)
        {
            throw new ConfigurationException("Illegal access instantiating class '" + clazz + "'");
        }

        // find method : static DataSource createDataSource(Properties properties)
        Method method;
        try
        {
            method = clazz.getMethod("createDataSource", Properties.class);
        }
        catch (NoSuchMethodException e)
        {
            throw new ConfigurationException("Class '" + clazz + "' does not provide a static method by name createDataSource accepting a single Properties object as parameter");
        }
        if (method == null)
        {
            throw new ConfigurationException("Class '" + clazz + "' does not provide a static method by name createDataSource accepting a single Properties object as parameter");            
        }
        if (method.getReturnType() != DataSource.class)
        {
            throw new ConfigurationException("On class '" + clazz + "' the static method by name createDataSource does not return a DataSource");                        
        }

        Object result;
        try
        {
            result = method.invoke(obj, dsConfig.getProperties());
        }
        catch (IllegalAccessException e)
        {
            throw new ConfigurationException("Class '" + clazz + "' failed in method createDataSource :" + e.getMessage(), e);
        }
        catch (InvocationTargetException e)
        {
            throw new ConfigurationException("Class '" + clazz + "' failed in method createDataSource :" + e.getMessage(), e);
        }
        if (result == null)
        {
            throw new ConfigurationException("Method createDataSource returned a null value for DataSource");
        }

        dataSource = (DataSource) result;
    }

    public Connection getConnection() throws DatabaseConfigException
    {
        Connection connection;
        try
        {
            connection = dataSource.getConnection();
        }
        catch (SQLException ex)
        {
            String detail = "SQLException: " + ex.getMessage() +
                    " SQLState: " + ex.getSQLState() +
                    " VendorError: " + ex.getErrorCode();

            throw new DatabaseConfigException("Error obtaining database connection using datasource " +
                    "with detail " + detail
                    , ex);
        }

        DatabaseDMConnFactory.setConnectionOptions(connection, connectionSettings);

        return connection;
    }
}

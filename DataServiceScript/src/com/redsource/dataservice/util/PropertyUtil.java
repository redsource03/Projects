package com.redsource.dataservice.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PropertyUtil {
	static Properties prop=null;

	public static Properties getProperty() {
		if(prop!=null) {
			return prop;
		}else {
			try {
			System.out.println("Loading Properties...");
			String filename= "config.properties";
			prop = new Properties();
			InputStream input = new FileInputStream(filename);
			prop.load(input);
			}catch(IOException e) {
				System.out.println("Unable to load config.property");
				e.printStackTrace();
			}finally {
				return prop;
			}
		}
	}
	
	public static Connection getConnectionStaging() {
    	Connection connection = null;
    	try {
    		Properties prop  = PropertyUtil.getProperty();
    		connection = DriverManager
    		.getConnection(prop.getProperty("CONNECTION_URL_STAGING"),prop.getProperty("DB_USER"), prop.getProperty("DB_PASS_STAGING"));

    	} catch (SQLException e) {
    		System.out.println("Connection Failed! Check output console");
    		e.printStackTrace();
    		return null;
    	}finally {
    		
    	}

    	if (connection != null) {
    		System.out.println("Connection Successful:STAGING");
    	} else {
    		System.out.println("Failed to make connection!");
    	}
       return connection;
    }
	public static Connection getConnectionProduction() {
    	Connection connection = null;
    	try {
    		Properties prop  = PropertyUtil.getProperty();
    		connection = DriverManager
    		.getConnection(prop.getProperty("CONNECTION_URL_PRODUCTION"),prop.getProperty("DB_USER"), prop.getProperty("DB_PASS_PRODUCTION"));

    	} catch (SQLException e) {
    		System.out.println("Connection Failed! Check output console");
    		e.printStackTrace();
    		return null;
    	}finally {
    		
    	}

    	if (connection != null) {
    		System.out.println("Connection Successful:PRODUCTION");
    	} else {
    		System.out.println("Failed to make connection!");
    	}
       return connection;
    }
	public static Connection getConnectionProductionXML() {
    	Connection connection = null;
    	try {
    		Properties prop  = PropertyUtil.getProperty();
    		connection = DriverManager
    		.getConnection(prop.getProperty("CONNECTION_URL_PRODUCTION_XML"),prop.getProperty("DB_USER"), prop.getProperty("DB_PASS_PRODUCTION_XML"));

    	} catch (SQLException e) {
    		System.out.println("Connection Failed! Check output console");
    		e.printStackTrace();
    		return null;
    	}finally {
    		
    	}

    	if (connection != null) {
    		System.out.println("Connection Successful:PRODUCTION_XML");
    	} else {
    		System.out.println("Failed to make connection!");
    	}
       return connection;
    }
}

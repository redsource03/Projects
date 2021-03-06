package com.redsource.dataservice;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.Session;
import com.redsource.dataservice.util.PropertyUtil;
import com.redsource.dataservice.util.SSHUtil;

public class UserCreation {
	private static final String SELECT_USER_PRD = "SELECT * FROM juwai.user WHERE ID=?";
	private static final String SELECT_USER_STG = "SELECT * FROM juwai.user WHERE email=?";
	private static final String UPDATE_JP_USER = "UPDATE user SET JPID=?, JP_GROUP=? where ID=?";
	private static final String INSERT_SOURCE_USER_MAPPING = "INSERT INTO juwai.source_user_mapping (USER_ID,SOURCE,SOURCE_AGENTID) VALUES (?,?,?)";
	private static final String INSERT_XML_CONFIG_SOURCES = "INSERT INTO xml.xml_config_sources (SOURCE,FEED_FORMAT_ID,ACCOUNT_ID,STATUS) VALUES (?,?,?,?)";
	private static final Exception Exception = new Exception();
	private static int USER_ID;
	private static int JPID;
	private static int JP_GROUP;
	private static String SOURCE;
	private static String QUEUE_NAME;
	private static String SOURCE_AGENT_ID;
	private static int FEED_FORMAT_PR;
	private static int FEED_FORMAT_ST;
	private static String EMAIL;
	private static int USER_ID_STAGING;
	

	public static void main(String[] args) throws SQLException {
		Connection dbPRDconn=null ;
		Connection dbSTGconn=null;
		Connection dbPRDXMLconn=null;
	    try {
	    dbPRDconn = PropertyUtil.getConnectionProduction();
	    	dbSTGconn = PropertyUtil.getConnectionStaging();
	    	dbPRDXMLconn = PropertyUtil.getConnectionProductionXML();
	    	loadPoperties();
	   // createQueue(); //working
	    copyUserPR2STG(dbPRDconn,dbSTGconn);
	    //insertSourceUserMapping(dbPRDconn, dbSTGconn);
	    //insertXMLconfigSources(dbPRDXMLconn, dbSTGconn);
	    }catch(Exception e) {
	    		e.printStackTrace();
			
	    }finally {
	    		dbPRDconn.close();
			dbSTGconn.close();
			dbPRDXMLconn.close();
		}

	}
	private static void insertXMLconfigSources(Connection dbPRDXMLconn,Connection dbSTGconn) throws SQLException {
		PreparedStatement stmtPRDXML=dbPRDXMLconn.prepareStatement(INSERT_XML_CONFIG_SOURCES);
		PreparedStatement stmtSTG=dbSTGconn.prepareStatement(INSERT_XML_CONFIG_SOURCES);
		
		try {
			stmtPRDXML.setString(1, SOURCE);
			stmtPRDXML.setInt(2, FEED_FORMAT_PR);
			stmtPRDXML.setInt(3, USER_ID);
			stmtPRDXML.setString(4, "Y");
			stmtPRDXML.execute();
				
			stmtSTG.setString(1, SOURCE);
			stmtSTG.setInt(2, FEED_FORMAT_ST);
			stmtSTG.setInt(3, USER_ID_STAGING);
			stmtSTG.setString(4, "Y");
			stmtSTG.execute();
			System.out.println("insertXMLconfigSources:OKAY");
		}catch(Exception e) {
			System.out.println("ERROR on inserting on xml_config_sources table");
			e.printStackTrace();
		}finally {
			stmtPRDXML.close();
			stmtSTG.close();
			
		}
	}
	private static void insertSourceUserMapping(Connection dbPRDconn,Connection dbSTGconn) throws SQLException{
		PreparedStatement stmtPRD=dbPRDconn.prepareStatement(INSERT_SOURCE_USER_MAPPING);
		PreparedStatement stmtSTG=dbSTGconn.prepareStatement(SELECT_USER_STG);
		ResultSet rs=null;
		try {
			stmtPRD.setInt(1, USER_ID);
			stmtPRD.setString(2, SOURCE);
			stmtPRD.setString(3, SOURCE_AGENT_ID);
			stmtPRD.execute();
				
			
			stmtSTG.setString(1, EMAIL); //get USER_ID ON STAGING
			rs = stmtSTG.executeQuery();
			rs.next();
			USER_ID_STAGING=rs.getInt("id");
			stmtSTG.close();
			stmtSTG=dbSTGconn.prepareStatement(INSERT_SOURCE_USER_MAPPING);
			stmtSTG.setInt(1, USER_ID_STAGING);
			stmtSTG.setString(2, SOURCE);
			stmtSTG.setString(3, SOURCE_AGENT_ID);
			stmtSTG.execute();
				
			System.out.println("insertSourceUserMapping:OKAY");
		}catch(Exception e) {
			System.out.println("ERROR on inserting on source_user_mapping table");
			e.printStackTrace();
		}finally {
			stmtPRD.close();
			rs.close();
			stmtSTG.close();
			
		}
	}
	private static void createQueue() {
		Properties prop = PropertyUtil.getProperty();
		Session session = SSHUtil.getConnection(prop.getProperty("PRODUCTION_SPLIT_HOST"), 
    			prop.getProperty("UNIX_USER"), prop.getProperty("PRODUCTION_PEM"));
		System.out.println(SSHUtil.executeCommand(session, createQueueCommand("staging"))); //CREATING STAGING
		System.out.println(SSHUtil.executeCommand(session, createQueueCommand("production"))); //CREATING PRODUCTION
		session.disconnect();
		System.out.println("createQueue:OKAY");
	}
	@SuppressWarnings("resource")
	private static void copyUserPR2STG(Connection dbPRDconn,Connection dbSTGconn) throws SQLException{
	
		PreparedStatement stmt=null;
		Statement stmtStg=null;
		try {
			//****UPDATE JPID,JPGROUP START *///
			
			stmt=dbPRDconn.prepareStatement(UPDATE_JP_USER);
			stmt.setInt(1, JPID);
			stmt.setInt(2, JP_GROUP);
			stmt.setInt(3, USER_ID);
			int count=stmt.executeUpdate();
			if(count!=1) {
				System.out.println("Something is Wrong on update JPID and JP_GROUP, Number of Updates:"+count);
				throw Exception;
			}
			//****UPDATE JPID,JPGROUP END *///
			stmt=dbPRDconn.prepareStatement(SELECT_USER_PRD);
			stmt.setInt(1, USER_ID);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			ResultSetMetaData meta = rs.getMetaData();
			List<String> columns = new ArrayList<>();
			StringBuilder columnNames = new StringBuilder();
			StringBuilder bindVariables = new StringBuilder();
			EMAIL=rs.getString("email");

			   for (int i = 1; i <= meta.getColumnCount(); i++) {
				   columns.add(meta.getColumnName(i));

			       if (i > 1) {
			           columnNames.append(", ");
			           bindVariables.append(", ");
			       }

			       columnNames.append(meta.getColumnName(i));
			       Object obj = rs.getObject(meta.getColumnName(i));
			       if(obj instanceof String){
			    	   String s = obj.toString().replaceAll("'", "\'");
			    	   	bindVariables.append("'"+s+"'");
			       }else if(obj instanceof Timestamp) {
			    	   	bindVariables.append("'"+obj.toString()+"'");
			       }
			       else if(obj == null) {
			    		bindVariables.append("NULL");
			       }
			    	   else bindVariables.append(rs.getObject(meta.getColumnName(i)));
			   }

			   String sql = "INSERT INTO " + "juwai.user" + " ("
			              + columnNames
			              + ") VALUES ("
			              + bindVariables
			              + ")";
			   stmtStg = dbSTGconn.createStatement();
			   stmtStg.execute(sql);
			   
			   System.out.println(sql);
			System.out.println("COPY of User information from Production to Staging is successful!");
		}catch(Exception e) {
			System.out.println("OOPSS something went wrong on Copy USER from PRD to STAGING");
			e.printStackTrace();
			
		}finally {
			stmt.close();
			stmtStg.close();
		}
	
		
	}
	private static String createQueueCommand(String environemnt) {
		Properties prop = PropertyUtil.getProperty();
		return prop.getProperty("CREATE_QUEUE")+" "+QUEUE_NAME+" "+environemnt;
	}
	
	private static  void loadPoperties() {
		Properties prop = PropertyUtil.getProperty();
		USER_ID =  Integer.parseInt(prop.getProperty("USER_CREATION_USER_ID"));
		JPID =  Integer.parseInt(prop.getProperty("USER_CREATION_JPID"));
		JP_GROUP =  Integer.parseInt(prop.getProperty("USER_CREATION_JP_GROUP"));
		SOURCE = prop.getProperty("USER_CREATION_SOURCE");
		QUEUE_NAME= prop.getProperty("USER_CREATION_QUEUE_NAME");
		FEED_FORMAT_PR =  Integer.parseInt(prop.getProperty("USER_CREATION_FEED_FORMAT_PR"));
		FEED_FORMAT_ST =  Integer.parseInt(prop.getProperty("USER_CREATION_FEED_FORMAT_ST"));
		SOURCE_AGENT_ID = prop.getProperty("USER_CREATION_SOURCE_AGENT_ID");
	}

}

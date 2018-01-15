package com.redsource.dataservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.jcraft.jsch.Session;
import com.redsource.dataservice.util.PropertyUtil;
import com.redsource.dataservice.util.SSHUtil;

public class MissingListingDiagnosis {
	private static final String SEARCH_PROPERTY_TABLE = "SELECT * FROM juwai.property WHERE SOURCE=? and SOURCE_ID=?";
	private static final String SEARCH_PROCESS_TABLE_ARC = "SELECT * FROM xml.xml_process_archive WHERE SOURCE=? and SOURCE_ID=? order by start_time desc ";
	private static final String SEARCH_PROCESS_TABLE = "SELECT * FROM xml.xml_process WHERE SOURCE=? and SOURCE_ID=? order by start_time desc ";
	private static final String SEARCH_STATUS_CHANGE = "SELECT * FROM juwai.property_status_changes where property_id=? order by created_at desc limit 1";
	private static final String UPDATE_PROPERTY_STATUS = "UPDATE property set property_save_status=1 where id=?";
	private static final String INSERT_STATUS_CHANGE = "INSERT into property_status_changes (property_id,reason,reason_id,new_status,last_status) values (?,?,?,?,?)";
	private static int PROPERTY_ID;
	private static int PROPERTY_SAVE_STATUS;
	private static String SOURCE;
	private static String SOURCE_ID;
	public static void main(String[] args) throws Exception {
		Connection dbPRDconn=null ;
		Connection dbPRDXMLconn=null;
		Session session=null;
	    try {
	    dbPRDconn = PropertyUtil.getConnectionProduction();
	    	dbPRDXMLconn = PropertyUtil.getConnectionProductionXML();
	    	Properties propConfig = PropertyUtil.getProperty();
	    	session = SSHUtil.getConnection(propConfig.getProperty("PRODUCTION_SPLIT_HOST"),
					propConfig.getProperty("UNIX_USER"), propConfig.getProperty("PRODUCTION_PEM"));
	    	if(searchPropertyTable(propConfig, dbPRDconn)) {
	    		if(PROPERTY_SAVE_STATUS!=1) {
	    			System.out.println("PROPERTY IS EXPIRED ");
	    			searchStatusChangeTable(dbPRDconn);
	    			
	    			BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
	    			System.out.println("Would you like to Manually activate? (y/n) : ");
	    			String input = br.readLine();
	    			if(input.equals("y")) {
	    				System.out.println("Please Provide Reason : ");
		    			input = br.readLine();
		    			System.out.println(input);
		    			//updatePropertyStatus(dbPRDconn, input);
	    			}else {
	    				System.out.println("Bye!");
	    			}
	    			

	    		}
	    	}else {
	    		System.out.println("LISTING IS NOT ON PROPERTY TABLE, CHECKING XML_PROCESS TABLE");
	    		if(checkProcessTable(dbPRDXMLconn)) {
	    			System.out.println("FEED:"+feedSearch(SOURCE, SOURCE_ID, session, propConfig));
	    		}else {
	    			System.out.println("LISTING DOESN'T HAVE DATABASE ENTRY: SEARCHING FOR FEED");
	    			System.out.println("FEED:"+feedSearch(SOURCE, SOURCE_ID, session, propConfig));
	    		}
	    		
	    	}
	    }catch(Exception e) {
	    		e.printStackTrace();
	    }finally {
	    		dbPRDconn.close();
	    		dbPRDXMLconn.close();
	    		session.disconnect();
		}
	
	}
	private static void updatePropertyStatus(Connection dbPRDconn,String reason)  throws SQLException{
		
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			stmt=dbPRDconn.prepareStatement(UPDATE_PROPERTY_STATUS);
			stmt.setInt(1, PROPERTY_ID);
			if(stmt.executeUpdate()!=1) {
				System.out.println("SOMETHING WRONG ON update property_change Status");
				throw new Exception();
			}else {
				stmt.close();
				stmt=dbPRDconn.prepareStatement(INSERT_STATUS_CHANGE);
				stmt.setInt(1, PROPERTY_ID);
				stmt.setString(2, reason);
				stmt.setInt(3, 11);
				stmt.setInt(4, 1);
				stmt.setInt(5, PROPERTY_SAVE_STATUS);
				if(!stmt.execute()) {
					System.out.println("SOMETHING WRONG ON insert property_change Status");
					throw new Exception();
				}
			}
		}catch (Exception e) {
			System.out.println("Problem  in updatePropertyStatic");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		
	}
	private static boolean checkProcessTable(Connection dbPRDXML) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			stmt=dbPRDXML.prepareStatement(SEARCH_PROCESS_TABLE);
			stmt.setString(1, SOURCE);
			stmt.setString(2, SOURCE_ID);
			rs=stmt.executeQuery();
			boolean found=false;
			while(rs.next()) {
				found=true;
				System.out.println("START_TIME:" +rs.getString("start_time") 
				+", MAP_TIME:"+rs.getString("MAP_TIME")
				+", CHECK_API_TIME:"+rs.getString("CHECK_API_TIME")
				+", IMAGE_TIME:"+rs.getString("IMAGE_TIME")
				+", SAVE_SG_TIME:"+rs.getString("SAVE_SG_TIME")
				+", FINISH_TIME:"+rs.getString("FINISH_TIME")
				+", ERROR_DETAIL:"+rs.getString("ERROR_DETAIL")
				+", ERROR_DESCRIPTION:"+rs.getString("ERROR_DESCRIPTION"));
			}
			if(!found){
				System.out.println("Listing is not on process table, searcing for process archive");
				stmt.close();
				rs.close();
				stmt=dbPRDXML.prepareStatement(SEARCH_PROCESS_TABLE_ARC);
				stmt.setString(1, SOURCE);
				stmt.setString(2, SOURCE_ID);
				rs=stmt.executeQuery();
				found=false;
				while(rs.next()) {
					found=true;
					System.out.println("START_TIME:" +rs.getString("start_time") 
					+", MAP_TIME:"+rs.getString("MAP_TIME")
					+", CHECK_API_TIME:"+rs.getString("CHECK_API_TIME")
					+", IMAGE_TIME:"+rs.getString("IMAGE_TIME")
					+", SAVE_SG_TIME:"+rs.getString("SAVE_SG_TIME")
					+", FINISH_TIME:"+rs.getString("FINISH_TIME")
					+", ERROR_DETAIL:"+rs.getString("ERROR_DETAIL")
					+", ERROR_DESCRIPTION:"+rs.getString("ERROR_DESCRIPTION"));
				}
				return found;
				
			}
		}catch (Exception e) {
			System.out.println("Problem  in checkProcessTable");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return true;
	}  
	private static String searchStatusChangeTable(Connection dbConn) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			stmt=dbConn.prepareStatement(SEARCH_STATUS_CHANGE);
			stmt.setInt(1, PROPERTY_ID);
			rs=stmt.executeQuery();
			if(rs.next()) {
				System.out.println("REASON:" +rs.getString("reason") 
				+", Date:"+rs.getString("created_at"));
			}
		}catch (Exception e) {
			System.out.println("Problem  in searchStatusChangeTable");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return "";
	}
	private static boolean searchPropertyTable(Properties prop, Connection dbConn) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			SOURCE=prop.getProperty("MISSING_LIST_SOURCE");
			SOURCE_ID=prop.getProperty("MISSING_LIST_SOURCE_ID");
			stmt=dbConn.prepareStatement(SEARCH_PROPERTY_TABLE);
			stmt.setString(1, SOURCE);
			stmt.setString(2, SOURCE_ID);
			rs= stmt.executeQuery();
			if(!rs.next()) {
				return false;
			}
			PROPERTY_ID= rs.getInt("id");
			PROPERTY_SAVE_STATUS = rs.getInt("property_save_status");
			System.out.println("PROPERTY_ID:" + PROPERTY_ID 
					+", PROPERTY_SAVE_STATUS:"+ PROPERTY_SAVE_STATUS
					+", ACCOUNT_ID:" + rs.getString("ACCOUNT_ID")
					+", SOURCE_AGENTID:"+ rs.getString("SOURCE_AGENTID"));
		}catch(Exception e) {
			System.out.println("PROBLEM in searching in property table");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return true;
	}
	public static String feedSearch(String source, String source_id,Session session, Properties propConfig) throws Exception{
		
		String command ="cd "+propConfig.getProperty("MISSING_LIST_FEED_LOC")+source+" && "+ propConfig.getProperty("MISSING_LIST_GREP_COMMAND")+" \">"+ source_id+"<\" * ";
		String result = SSHUtil.findLatestFeed(session, command);
		if(result.equals("")) {
			result="NO FEED FOUND!";
		}
		return result;
	}
}

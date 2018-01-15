package com.redsource.dataservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsource.dataservice.util.PropertyUtil;

public class DS1440 {
	private static final String UPDATE_PROPERTY_TABLE ="UPDATE property SET SOURCE_ID = ?, SOURCE_AGENTID=? WHERE SOURCE=? and SOURCE_ID=?";
	private static final String UPDATE_PROPERTY_TABLE_NO_NEW_SOURCE_GENTID= "UPDATE property SET SOURCE_ID = ? WHERE SOURCE=? and SOURCE_ID=?";
	private static final String GET_SOURCE_AGENTID= "SELECT SOURCE_AGENTID FROM property WHERE SOURCE=? and SOURCE_ID=?";
	public static void main(String[] args) throws Exception{
		File f = new File("ds1440office.csv");
		Connection dbPRDconn=null ;
		 Set<String> missing = new HashSet<String>();
		HashMap<String, String> officeMap = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	List<String> list = Arrays.asList(line.split(","));
		    	officeMap.put(list.get(0).trim(), list.get(1).trim());
		    }
		}
		try{
			dbPRDconn = PropertyUtil.getConnectionProduction();
			int counter=0;
			f = new File("ds1440list.csv");
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	List<String> list = Arrays.asList(line.split(","));
			    	String newSourceAgentId = getSourceAgentID(dbPRDconn, list.get(0).trim());
			    	if(newSourceAgentId.equals("not found")) {
			    		continue;
			    	}

			    	System.out.print(counter+":");
			    	if(officeMap.get(newSourceAgentId.trim())==null){
			    		missing.add(newSourceAgentId.trim());
			    		updateListHubNoAgent(dbPRDconn, list.get(0).trim(), list.get(1).trim());
			    	}else{
			    		updateListHub(dbPRDconn, list.get(0).trim(), list.get(1).trim(), officeMap.get(newSourceAgentId.trim()));
			    	}
			    	
			    	counter++;
			    	/*if (counter==1000){
			    		break;
			    	}*/
			    }
			    System.out.println(missing.toString());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbPRDconn.close();
		}
	}
	private static void updateListHub(Connection dbPRDconn,String oldSourceId,String newSourceId, String newSourceAgentId)throws SQLException{
		PreparedStatement stmt=null;
		try{
			stmt=dbPRDconn.prepareStatement(UPDATE_PROPERTY_TABLE);
			stmt.setString(1, newSourceId);
			stmt.setString(2, newSourceAgentId);
			stmt.setString(3, "listhub3");
			stmt.setString(4, oldSourceId);
			
			if(stmt.executeUpdate()==1){
				System.out.println("UPDATE PROPERTY SET SOURCE_ID = '"+newSourceId +"', SOURCE_AGENTID='"+newSourceAgentId+"' WHERE SOURCE='listhub3' and SOURCE_ID='"+oldSourceId+"'");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			stmt.close();
		}
		
	}
	private static void updateListHubNoAgent(Connection dbPRDconn,String oldSourceId,String newSourceId)throws SQLException{
		PreparedStatement stmt=null;
		try{
			stmt=dbPRDconn.prepareStatement(UPDATE_PROPERTY_TABLE_NO_NEW_SOURCE_GENTID);
			stmt.setString(1, newSourceId);
			stmt.setString(2, "listhub3");
			stmt.setString(3, oldSourceId);
			if(stmt.executeUpdate()==1){
				System.out.println("UPDATE PROPERTY SET SOURCE_ID = '"+newSourceId +"'"+" WHERE SOURCE='listhub3' and SOURCE_ID='"+oldSourceId+"'");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			stmt.close();
		}
		
	}
	private static String getSourceAgentID(Connection dbPRDconn,String oldSourceId) throws Exception{
		PreparedStatement stmt=null;
		ResultSet rs = null;
		String source_agentId="not found";
		try{
			stmt=dbPRDconn.prepareStatement(GET_SOURCE_AGENTID);
			stmt.setString(1, "listhub3");
			stmt.setString(2, oldSourceId);
			rs=stmt.executeQuery();
			if(rs.next()){
				source_agentId=rs.getString("SOURCE_AGENTID");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			stmt.close();
		}
		return source_agentId;
	}

}

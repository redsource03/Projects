package com.redsource.dataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.redsource.dataservice.util.PropertyUtil;




public class UpdatePropertyLocation {
	private static final String UPDATE_PROPERTY_TABLE ="UPDATE PROPERTY SET COUNTRY_ID = ?, REGION_ID=? , CITY_ID=? WHERE SOURCE=? and SOURCE_ID=?";
	private static final String SELECT_REGION = "SELECT ID FROM region WHERE COUNTRY_ID=? and NAME=?";
	private static final String SELECT_CITY ="SELECT ID FROM city WHERE COUNTRY_ID=? and NAME=?";
	
	private static final String SELECT_ALIAS_REGION= "SELECT ORIGINAL_NAME FROM location_aliases WHERE ALIAS_NAME=? and TYPE='region'";
	private static final String SELECT_ALIAS_CITY="SELECT ORIGINAL_NAME FROM location_aliases WHERE ALIAS_NAME=? and TYPE='locality'";
	private static final String FORCE_REGION_ID="SELECT REGION_ID FROM city WHERE id=? ";
	
	private static final String GET_COUNTRY_ID_1= "SELECT ID FROM juwai.country where name=?";
	private static final String GET_COUNTRY_ID_2= "SELECT ID FROM juwai.country where iso2=?";
	private static final String GET_COUNTRY_ID_3= "SELECT ID FROM juwai.country where iso3=?";
	private static final String GET_COUNTRY_ID_4= "SELECT ID FROM juwai.country where fips104=?";
	
	private static final String GET_COUNTRY_ID = "SELECT COUNTRY_ID FROM juwai.property where source=? and source_id=?";
	private static  String CONNECTION_URL;
	private static  String DB_USER;
	private static  String DB_PASS;
	private static  String SOURCE;

	public static  int COUNTRY_ID;
	
	private static boolean updatePropertyTable(String source, String source_id,int country_id,int region_id, int city_id,Connection con) throws Exception{
		PreparedStatement stmt=con.prepareStatement(UPDATE_PROPERTY_TABLE);
		stmt.setInt(1, country_id);
		stmt.setInt(2, region_id);
		stmt.setInt(3, city_id);
		stmt.setString(4, SOURCE);
		stmt.setString(5, source_id);
		if(stmt.executeUpdate()!= 1)  return false;
		
		return true;
	}
	private static void loadProperties() throws FileNotFoundException,IOException{
		Properties prop  = PropertyUtil.getProperty();
		CONNECTION_URL=prop.getProperty("CONNECTION_URL_PRODUCTION");
		DB_USER=prop.getProperty("DB_USER");
		DB_PASS=prop.getProperty("DB_PASS_PRODUCTION");
		SOURCE=prop.getProperty("SOURCE");
		//COUNTRY_ID=Integer.parseInt(prop.getProperty("COUNTRY_ID"));
	}
	private static int checkIfSameCountry(Connection con,String source, String source_id)throws Exception{
		PreparedStatement stmt=con.prepareStatement(GET_COUNTRY_ID);
		stmt.setString(1, source);
		stmt.setString(2, source_id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			if(rs.getInt("COUNTRY_ID")==COUNTRY_ID){
				return 0;
			}
		}
		else{
			return -1;
		}
		rs.close();
		stmt.close();
		return 1;
	}
	private static int getCountryId(Connection con,String country)throws Exception{
		PreparedStatement stmt=con.prepareStatement(GET_COUNTRY_ID_1);
		stmt.setString(1, country);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return rs.getInt("id");
		}else{
			stmt=con.prepareStatement(GET_COUNTRY_ID_2);
			stmt.setString(1, country);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt("id");
			}else{
				stmt=con.prepareStatement(GET_COUNTRY_ID_3);
				stmt.setString(1, country);
				rs = stmt.executeQuery();
				if(rs.next()){
					return rs.getInt("id");
				}else{
					stmt=con.prepareStatement(GET_COUNTRY_ID_4);
					stmt.setString(1, country);
					rs = stmt.executeQuery();
					if(rs.next()){
						return rs.getInt("id");
					}
				}
			}
		}
		stmt.close();
		rs.close();
		return 0;
	}
	
	public static int getRegionForce(int city_id,Connection con) throws Exception{
		int region_ID=0;
		PreparedStatement stmt=con.prepareStatement(FORCE_REGION_ID);
		stmt.setInt(1, city_id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {// get first value
			region_ID=rs.getInt("REGION_ID");
		}
		rs.close();
		stmt.close();
		return region_ID;
	}
	public static int getRegion(String region,Connection con) throws Exception{
		PreparedStatement stmt=con.prepareStatement(SELECT_REGION);
		stmt.setInt(1, COUNTRY_ID);
		stmt.setString(2,region);
		int region_ID=0;
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {// get first value
			region_ID=rs.getInt("ID");
		}else {
			region_ID=getRegionAlias(region, con);
		}
		stmt.close();
		rs.close();
		return region_ID;
	}
	private static int getRegionAlias(String region, Connection con) throws Exception{
		PreparedStatement stmt=con.prepareStatement(SELECT_ALIAS_REGION);
		stmt.setString(1,region);
		int region_ID=0;
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			PreparedStatement stmt2=con.prepareStatement(SELECT_REGION);
			stmt2.setInt(1, COUNTRY_ID);
			stmt2.setString(2,rs.getString("ORIGINAL_NAME"));
			ResultSet rs2 = stmt2.executeQuery();
			if(rs2.next()) {
				region_ID=rs2.getInt("id");
				break;
			}else {
				region_ID=0;
			}
			rs2.close();
			stmt2.close();
		}
		
		stmt.close();
		return region_ID;
	}
	public static void main(String[] args) throws Exception {
        File f = new File("UpdatePropertyLocation.json");

		PrintWriter writer = new PrintWriter("UpdatePropertyLocation_Result.txt", "UTF-8");
        loadProperties();
        if (f.exists()){
        
            InputStream is = new FileInputStream("file_production.json");
            String jsonTxt = IOUtils.toString(is);
            //System.out.println(jsonTxt);
            
            try {
        		Class.forName("com.mysql.jdbc.Driver");
	        	} catch (ClassNotFoundException e) {
	        		System.out.println("Missing MySQL JDBC Driver");
	        		e.printStackTrace();
	        		return;
	        	}

	        	System.out.println("MySQL JDBC Driver Registered!");
	        	Connection con=  getConnection();
            JSONArray jsonArr = new JSONArray(jsonTxt);
            Set<String> missing = new HashSet<String>();
            try {
	            for(int i=0;i<jsonArr.length();i++) {
	            		JSONObject jobj=jsonArr.getJSONObject(i);
	            		COUNTRY_ID=getCountryId(con, jobj.getString("country"));
	            		if(checkIfSameCountry(con, SOURCE, jobj.getString("source_id"))==1){ //1 - not same, 0 - same, -1 does not exist
		            		SOURCE=jobj.getString("source");
		            		int region_ID=getRegion(jobj.getString("region"), con);
		            		int city_ID=getCity(jobj.getString("locality"), con);
		            		if(region_ID==0 && city_ID!=0) {// IF NO REGION on FEED get region_id from city
		            			region_ID=getRegionForce(city_ID, con);
		            		}
		            		if(city_ID==0) {
		            			missing.add("Missing City:"+jobj.getString("locality"));
		            		}
		                /*System.out.println("COUNT:"+i+" SOURCE_ID:"+ jobj.getString("source_id")+
		                		" COUNTRY_ID:" + COUNTRY_ID +
		                		" REGION_ID:"+ region_ID +
		                		" CITY_ID:" + city_ID); */
		                
		                
		                	System.out.println("UPDATE property SET COUNTRY_ID ="+ COUNTRY_ID+", REGION_ID="+region_ID+", CITY_ID="+city_ID+" WHERE SOURCE_ID='"+jobj.getString("source_id")+"' and SOURCE='"+SOURCE+"';");
			                writer.println("UPDATE property SET COUNTRY_ID ="+ COUNTRY_ID+", REGION_ID="+region_ID+", CITY_ID="+city_ID+" WHERE SOURCE_ID='"+jobj.getString("source_id")+"' and SOURCE='"+SOURCE+"';");
	            		}
	                System.out.println("Processed:"+i+"/"+jsonArr.length()) ;
	                	
	                
	            }
	            System.out.println(missing.toString());
            }catch(Exception e) {
            	e.printStackTrace();
            }finally {
            	writer.close();
	        	con.close();
            }
        }
	
	        
    
    }
	
	public static int getCity(String city,Connection con) throws Exception{
		PreparedStatement stmt=con.prepareStatement(SELECT_CITY);
		stmt.setInt(1, COUNTRY_ID);
		stmt.setString(2,city);
		int city_ID=0;
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {// get first value
			city_ID=rs.getInt("ID");
		}else {
			city_ID=getCityAlias(city, con);
		}
		stmt.close();
		rs.close();
		return city_ID;
	}
	private static int getCityAlias(String city, Connection con) throws Exception{
		PreparedStatement stmt=con.prepareStatement(SELECT_ALIAS_CITY);
		stmt.setString(1,city);
		int city_ID=0;
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			PreparedStatement stmt2=con.prepareStatement(SELECT_CITY);
			stmt2.setInt(1, COUNTRY_ID);
			stmt2.setString(2,rs.getString("ORIGINAL_NAME"));
			ResultSet rs2 = stmt2.executeQuery();
			if(rs2.next()) {
				city_ID=rs2.getInt("id");
				break;
			}else {
				city_ID=0;
			}
			rs2.close();
			stmt2.close();
		}
		
		stmt.close();
		return city_ID;
	}
    
    private static Connection getConnection() {
        	Connection connection = null;
        	try {
        		connection = DriverManager
        		.getConnection(CONNECTION_URL,DB_USER, DB_PASS);

        	} catch (SQLException e) {
        		System.out.println("Connection Failed! Check output console");
        		e.printStackTrace();
        		return null;
        	}finally {
        		
        	}

        	if (connection != null) {
        		System.out.println("Connection Successful!");
        	} else {
        		System.out.println("Failed to make connection!");
        	}
           return connection;
        }
	
       
}

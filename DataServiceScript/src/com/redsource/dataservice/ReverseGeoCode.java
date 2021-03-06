package com.redsource.dataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jcraft.jsch.Session;
import com.redsource.dataservice.util.PropertyUtil;
import com.redsource.dataservice.util.SSHUtil;

public class ReverseGeoCode {

	public static void main(String[] args) throws Exception{
		File f = new File("reverse_geo_code.json");
		PrintWriter writer = new PrintWriter("ReverseGeoCode_Result.txt", "UTF-8");

	 
        if (f.exists()){
        
            InputStream is = new FileInputStream("reverse_geo_code.json");
            String jsonTxt = IOUtils.toString(is);
            Properties propConfig = PropertyUtil.getProperty();
	        	Connection con=  PropertyUtil.getConnectionProduction();
            JSONArray jsonArr = new JSONArray(jsonTxt);
            Set<String> missing = new HashSet<String>();
        		Session session = SSHUtil.getConnection(propConfig.getProperty("STAGING_SPLIT_HOST"),
					propConfig.getProperty("UNIX_USER"), propConfig.getProperty("STAGING_PEM"));
            
            try {
	            for(int i=0;i<jsonArr.length();i++) {
	            		JSONObject jobj=jsonArr.getJSONObject(i);
	            		String command  = propConfig.getProperty("REVERSE_GEO_CODE_SCRIPT_LOC")+" && "+ propConfig.getProperty("REVERSE_GEO_CODE_CMD")
	            		+ "'{\"street\": \""+jobj.getString("address")+"\",\"country\":\""+propConfig.getProperty("REVERSE_GEO_CODE_COUNTRY")+"\"}'";
	            		String s= SSHUtil.executeCommand(session, command);
	            		if(s.indexOf('{')==-1){
	            			continue;
	            		}
	            		JSONObject googsJsonObj = new JSONObject(s.substring(s.indexOf('{'), s.length()));
	            		if(googsJsonObj.getBoolean("valid_address") && googsJsonObj.getString("country").toLowerCase().equals(propConfig.getProperty("REVERSE_GEO_CODE_COUNTRY").toLowerCase())) {
	            			findAddress(googsJsonObj, jobj, con,writer);
	            		}else {
	            			command  = propConfig.getProperty("REVERSE_GEO_CODE_SCRIPT_LOC")+" && "+ propConfig.getProperty("REVERSE_GEO_CODE_CMD")
		            		+ "'{\"street\": \""+jobj.getString("address").replaceAll("[0-9]","")+"\",\"country\":\""+propConfig.getProperty("REVERSE_GEO_CODE_COUNTRY")+"\"}'";
	            			s= SSHUtil.executeCommand(session, command);
	            			googsJsonObj = new JSONObject(s.substring(s.indexOf('{'), s.length()));
	            			if(googsJsonObj.getBoolean("valid_address")) {
	            				findAddress(googsJsonObj, jobj, con,writer);
	            			}else {
	            				missing.add(jobj.getInt("property_id")+"");
	            				//System.out.println("REMOVED ALL NUMBERS STILL ADDRESS IS NOT FOUND PROPERTY_ID:"+jobj.getInt("property_id") );
	            			}
	            		}
	            		
	            		
            }
	            System.out.println(missing.toString());
            }catch(Exception e) {
             	System.out.println("SOMETHING IS WRONG WOOOPS");
            	e.printStackTrace();
            }finally {
            session.disconnect();
	        	con.close();
	        	 writer.close();
            }
        }

	}
	private static void findAddress(JSONObject googsJsonObj,JSONObject jobj,Connection con,PrintWriter writer) throws Exception{
		UpdatePropertyLocation.COUNTRY_ID=jobj.getInt("country_id");
		int region_ID=UpdatePropertyLocation.getRegion(googsJsonObj.getString("region"), con);
		int city_ID=UpdatePropertyLocation.getCity(googsJsonObj.getString("locality"), con);
		if(region_ID==0 && city_ID!=0) {// IF NO REGION on FEED get region_id from city
			region_ID=UpdatePropertyLocation.getRegionForce(city_ID, con);
		}
		
    /*System.out.println("COUNT:"+i+" SOURCE_ID:"+ jobj.getString("source_id")+
    		" COUNTRY_ID:" + COUNTRY_ID +
    		" REGION_ID:"+ region_ID +
    		" CITY_ID:" + city_ID); */
    
    System.out.println("UPDATE property SET COUNTRY_ID ="+ jobj.getInt("country_id")+", REGION_ID="+region_ID+", CITY_ID="+city_ID
    		+", LAT='"+googsJsonObj.getDouble("lat")+"'"
    		+", `LONG`='"+googsJsonObj.getDouble("lng")+"'"
    		+", `lat_long_provider` = 7"
    		+" WHERE ID="+jobj.getInt("property_id")+";");
    
    writer.println("UPDATE property SET COUNTRY_ID ="+ jobj.getInt("country_id")+", REGION_ID="+region_ID+", CITY_ID="+city_ID
    		+", LAT='"+googsJsonObj.getDouble("lat")+"'"
    		+", `LONG`='"+googsJsonObj.getDouble("lng")+"'"
    		+", `lat_long_provider` = 7"
    		+" WHERE ID="+jobj.getInt("property_id")+";");
	}
	  
}

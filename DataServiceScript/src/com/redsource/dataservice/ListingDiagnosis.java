package com.redsource.dataservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Session;
import com.redsource.dataservice.util.PropertyUtil;
import com.redsource.dataservice.util.SSHUtil;

public class ListingDiagnosis {
	private final static String FEED_LOC="/data/provider/";
	private final static String GET_ALL_XML_CMD="-iname '*.xml' -printf '%p\n' | sort -k1 -n";
	private final static String EXTRACT_CMD="ve python -m jw.utils.extract ";
	private final static String VE_CMD="cd /usr/local/xml-project-scripts/ && source env/bin/activate && cd src ";
	
	
	private final static String DB_CONFIG_SOURCES="SELECT feed_format_id from xml.xml_config_sources where source=?";
	private final static String DB_FEED_FORMAT="SELECT * from  xml.xml_config_feed_formats where id=?";
	private final static String DB_PROPERTY = "select id from juwai.property where source=? and source_id=? and DATE(CHANGED)>=?";
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String[] fileNm= {"result_renet_january.csv","result_renet_september.csv","result_renet_october.csv","result_renet_november.csv","result_renet_december.csv"};
		combineResult(fileNm);
		Connection dbPRDconn=null ;
		Connection dbPRDXMLconn=null;
		Session session=null;
		PrintWriter writer = new PrintWriter("listingDiagnosis/result.csv", "UTF-8");
		HashMap<String,String> listingMap=new HashMap<String,String>(); 
		if(args.length==1 || args.length==3){
			try {
				dbPRDconn = PropertyUtil.getConnectionProduction();
				dbPRDXMLconn = PropertyUtil.getConnectionProductionXML();
		    	Properties propConfig = PropertyUtil.getProperty();
		    	session = SSHUtil.getConnection(propConfig.getProperty("PRODUCTION_SPLIT_HOST"),
						propConfig.getProperty("UNIX_USER"), propConfig.getProperty("PRODUCTION_PEM"));
		    	String folder = args[0].equalsIgnoreCase("raywhite_mydesktop") ? "raywhite":args[0];
		    	int feed_id=getFeedFormatID(dbPRDXMLconn, args[0]);
		    	if(feed_id==0){
		    		System.out.println("INVALID SOURCE");
		    		throw new Exception();
		    	}
		    	String[] feedType = getTypeFormat(dbPRDXMLconn, feed_id);
		    	String dateRange="";
		    	if(args.length==3){
		    		dateRange="-newermt '"+args[1]+"' ! -newermt '"+args[2]+"'";
		    	}
		    	String getAllXMLCMD="find "+FEED_LOC+folder+" "+dateRange+" "+GET_ALL_XML_CMD;
		    	System.out.println(getAllXMLCMD);
		    	String result = SSHUtil.executeCommand(session,getAllXMLCMD);
		    	String[] ary = result.split("\n");
		    	if(feedType[0].equalsIgnoreCase("incremental")){
		    		System.out.println("INCREMENTAL,Processing the Feed");
		    		for(int i=0;i<ary.length;i++){
		    			//String[] extractAry=SSHUtil.executeCommand(VE_CMD+ " && "+EXTRACT_CMD+folder+" "+ary[i]+" "+feedType[1]).split("\n");
		    			String s="cat "+ary[i]+" |"+"grep -iPo "+"'<"+feedType[1]+">(.|\\n)*?<\\/"+feedType[1]+">'";
		    			String[] extractAry=SSHUtil.executeCommand(session,s).split("\n");
		    			System.out.println("Processed:"+i+"/"+ary.length);
		    			for(int j=0;j<extractAry.length;j++){
		    				listingMap.put(extractAry[j].replaceAll("<"+feedType[1]+">", "").replaceAll("</"+feedType[1]+">", ""), ary[i]);
		    			}
		    			//if(i==10)break;
		    		}
		    	}else{
		    		System.out.println("SNAPSHOT,Processing the Feed");
		    		String s="cat "+ary[ary.length-1]+" |"+"grep -iPo "+"'<"+feedType[1]+">(.|\\n)*?<\\/"+feedType[1]+">'";
	    			String[] extractAry=SSHUtil.executeCommand(session,s).split("\n");
	    			//System.out.println("Processed:"+i+"/"+ary.length);
	    			for(int j=0;j<extractAry.length;j++){
	    				listingMap.put(extractAry[j].replaceAll("<"+feedType[1]+">", "").replaceAll("</"+feedType[1]+">", ""), ary[ary.length-1]);
	    			}
		    	}
		    	dbPRDconn = PropertyUtil.getConnectionProduction();
		    	dbPRDXMLconn = PropertyUtil.getConnectionProductionXML();
		    	///Iterate on map get the database
		    	int i=0;
		    	System.out.println("CHECKING DATABASE");
		    	for (Map.Entry<String, String> entry : listingMap.entrySet()){
		    	    if(!checkIfUpdated(dbPRDconn, args[0], entry.getKey(), StringUtils.substringBetween(entry.getValue(), folder+"/", "/"))){
		    	    	
		    	    	writer.println((entry.getKey() +","+entry.getValue()));
		    	    	
		    	    }
		    	    if(i%100==0){
		    	    	System.out.println(i+"/"+listingMap.size());
		    	    }
		    	    i++;
		    	}
		    	System.out.println("DONE!");
		    	
			}catch(Exception e){
		    	e.printStackTrace();
		    }finally {
	    		dbPRDconn.close();
	    		dbPRDXMLconn.close();
	    		session.disconnect();
	    		writer.close();
		    }
			
		}else{
			System.out.println("INVALID Arguments");
			System.out.println("<source> || <source> <date_1> <date_2>");
		}
	    
	}
	private static boolean checkIfUpdated(Connection dbPRDconn,String source, String sourceId, String date ) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			stmt=dbPRDconn.prepareStatement(DB_PROPERTY);
			stmt.setString(1, source);
			stmt.setString(2, sourceId);
			stmt.setString(3, date);
			rs=stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (Exception e) {
			System.out.println("Problem  in checkIfUpdated");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return false;
	}
	private static int getFeedFormatID(Connection dbPRDXMLconn, String source) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		try {
			stmt=dbPRDXMLconn.prepareStatement(DB_CONFIG_SOURCES);
			stmt.setString(1, source);
			rs=stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt("feed_format_id");
			}
		}catch (Exception e) {
			System.out.println("Problem  in getFeedFormatID");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return 0;
	}
	private static String[] getTypeFormat(Connection dbPRDXMLconn,int feedId) throws SQLException{
		ResultSet rs =null;
		PreparedStatement stmt=null;
		String[] arr=new String[2];
		try {
			stmt=dbPRDXMLconn.prepareStatement(DB_FEED_FORMAT);
			stmt.setInt(1, feedId);
			rs=stmt.executeQuery();
			if(rs.next()) {
				arr[0]=rs.getString("type");
				arr[1]=rs.getString("id_element");
				return arr;
			}
		}catch (Exception e) {
			System.out.println("Problem  in getTypeFormat");
			e.printStackTrace();
		}finally {
			rs.close();
			stmt.close();
		}
		return null;
	}
	private static void combineResult(String[] fileNm) throws Exception{
		File f;
		int i=0;
		PrintWriter writer = new PrintWriter("listingDiagnosis/result_final.csv", "UTF-8");
		HashMap<String, String> finalMap = new HashMap<String, String>();
		System.out.println("Transfering to Map");
		for( i=0;i<fileNm.length;i++){
			f = new File("listingDiagnosis/"+fileNm[i]);
			System.out.println("READING FILE:"+"listingDiagnosis/"+fileNm[i] );
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	List<String> list = Arrays.asList(line.split(","));
			    	finalMap.put(list.get(0).trim(), list.get(1).trim());
			    }
			}
		}
		i=0;
		System.out.println("CREATING FINAL RESULT");
		for (Map.Entry<String, String> entry : finalMap.entrySet()){
			writer.println((entry.getKey() +","+entry.getValue()));
    	    if(i%100==0){
    	    	System.out.println(i+"/"+finalMap.size());
    	    }
    	    i++;
    	}
		System.out.println("DONE!");
		writer.close();
		System.exit(0);
	}

}


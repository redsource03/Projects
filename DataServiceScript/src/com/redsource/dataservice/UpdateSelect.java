package com.redsource.dataservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.redsource.dataservice.util.PropertyUtil;

public class UpdateSelect {
	private final static String SELECT_NULL_IMAGE ="select id from juwai.property where source='betterhomes' and image is null";
	private final static String SELECT_URL = "select url from image where property_id=? order by 'order' asc limit  1";
	private final static String UPDATE_PROPERTY_IMG="update property set image=? where id=?";
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Connection dbPRDconn=null ;
		ResultSet rs =null;
		ResultSet rs2 =null;
		PreparedStatement stmt=null;
		try{
			dbPRDconn = PropertyUtil.getConnectionProduction();
			stmt=dbPRDconn.prepareStatement(SELECT_NULL_IMAGE);
			rs=stmt.executeQuery();
			while(rs.next()){
				stmt=dbPRDconn.prepareStatement(SELECT_URL);
				stmt.setInt(1, rs.getInt("id"));
				rs2=stmt.executeQuery();
				if(rs2.next()){
					stmt=dbPRDconn.prepareStatement(UPDATE_PROPERTY_IMG);
					stmt.setString(1,rs2.getString("url") );
					stmt.setInt(2, rs.getInt("id"));
					if(stmt.executeUpdate()==1){
						System.out.println("UPDATE ID:"+rs.getInt("id")+"\\URL:"+rs2.getString("url"));
					}else{
						System.out.println("ERROR:"+rs.getInt("id"));
					}
					
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
			dbPRDconn.close();
			rs.close();
			rs2.close();
			stmt.close();
		}
	}

}

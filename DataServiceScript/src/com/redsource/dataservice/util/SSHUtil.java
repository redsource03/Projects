package com.redsource.dataservice.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHUtil {
	public static Session getConnection(String host,String user, String identity) {
			Session session =null;
		    try{
			    	JSch jsch = new JSch();
			    	jsch.addIdentity(identity);
			    	java.util.Properties config = new java.util.Properties(); 
			    	config.put("StrictHostKeyChecking", "no");
			    	session=jsch.getSession(user, host, 22);
			    	session.setConfig(config);
			    	session.connect();
		    }catch(Exception e) {
		    	System.out.println("EXCEPTION HAPPENED: Unable to connect");
		    	e.printStackTrace();
		    }
		    	System.out.println("Connected to:"+host);
		    	return session;
	}
	public static  String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		String s= output.toString();
		output=null;
		System.out.println(s);
		return s;

	}
	public static String executeCommand(Session session, String command) {
		StringBuffer buff = new StringBuffer();
		try {
			Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        channel.connect();
	        byte[] tmp=new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            if(i!=0) buff.append(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            //System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }
	          //try{Thread.sleep(1000);}catch(Exception ee){}
	        }
	        channel.disconnect();
		}catch(Exception e) {
			System.out.println("ERROR ON EXECUTING COMMAND");
			e.printStackTrace();
		}
		String s= buff.toString();
		buff=null; // to free up memory
        return s;
	}
	public static String findLatestFeed(Session session, String command) {
		StringBuffer buff = new StringBuffer();
		ArrayList<String> list = new ArrayList<String>();
		try {
			Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        channel.connect();
	        byte[] tmp=new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            if(i!=0) buff.append(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            //System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }
	          try{Thread.sleep(1000);}catch(Exception ee){}
	        }
	        channel.disconnect();
		}catch(Exception e) {
			System.out.println("ERROR ON EXECUTING COMMAND");
			e.printStackTrace();
		}
		String s="";
		if(buff.length()==0) return "";
		for(int i=0;i<buff.length();i++) {
			if((buff.charAt(i)+"").equals("\n")) {
				list.add(s);
				s="";
			}else
				s+=buff.charAt(i)+"";
			
		}
		//list.sort(String::compareToIgnoreCase);
        return list.get(list.size()-1);
	}
}

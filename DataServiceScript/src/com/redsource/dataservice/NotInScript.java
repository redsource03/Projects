package com.redsource.dataservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class NotInScript {

	public static void main(String[] args) throws Exception{
		File f = new File("notinScript/juwai.txt");
		 Set<String> juwai = new HashSet<String>();
		 Set<String> notin = new HashSet<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
		    String line;
		    int counter=0;
		    while ((line = br.readLine()) != null) {
		    	counter++;
		    	juwai.add(line);
		    }
		}
		System.out.println(juwai.toString());
		f=new File("notinScript/feed.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
		    String line;
		    int counter=0;
		    while ((line = br.readLine()) != null) {
		    	if(!juwai.contains(line)){
		    		notin.add(line);
		    	}
		    }
		    //System.out.println(notin.toString());
		}

	}

}

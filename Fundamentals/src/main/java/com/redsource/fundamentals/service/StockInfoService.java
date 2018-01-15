package com.redsource.fundamentals.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redsource.fundamentals.hibernate.dao.GrowthDao;
import com.redsource.fundamentals.hibernate.dao.MetricsDao;
import com.redsource.fundamentals.hibernate.dao.StockDao;
import com.redsource.fundamentals.hibernate.dao.StockPriceDao;
import com.redsource.fundamentals.hibernate.model.StockPrice;
import com.redsource.fundamentals.rest.model.CompanyInfo;

@Service("stockInfoService")
public class StockInfoService {
	@Autowired
	GrowthDao growthDao;
	@Autowired
	StockDao stockDao;
	@Autowired
	MetricsDao metricsDao;
	@Autowired
	StockPriceDao stockPriceDao;
	
	@Transactional
	public CompanyInfo getStockInfo(String stock,String year){
		CompanyInfo com = new CompanyInfo();
		com.setGrowthModel(growthDao.getStockGrowth(stock));
		com.setStockModel(stockDao.getStockInfo(stock));
		com.setStockMetrics(metricsDao.getStockMetrics(stock, year));
		com.setStockPriceModel (new ArrayList(stockPriceDao.getStockPriceAll(stock)));
		return com;
	}
	@Transactional
	public  void load() throws Exception{
			
			// TODO Auto-generated method stub
			File f = new File("stock.json");
			if(f.exists()){
				InputStream is = new FileInputStream("stock.json");
	            String jsonTxt = IOUtils.toString(is);
	            JSONObject j = new  JSONObject(jsonTxt);
	            String stock = j.getString("stock");
	            JSONArray jsonArr = j.getJSONArray("chartData"); 
	            try {
		            for(int i=0;i<jsonArr.length();i++) {
		            	JSONObject jobj=jsonArr.getJSONObject(i);
		            	StockPrice sp = new StockPrice();
		            	sp.setOpen(Float.valueOf(jobj.getDouble("OPEN")+""));
		            	sp.setHigh(Float.valueOf(jobj.getDouble("HIGH")+""));
		            	sp.setLow(Float.valueOf(jobj.getDouble("LOW")+""));
		            	sp.setClose(Float.valueOf(jobj.getDouble("CLOSE")+""));
		            	SimpleDateFormat ft = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
		            	java.util.Date d= ft.parse(jobj.getString("CHART_DATE"));
		            	sp.setDate(d);
		            	sp.setStock(stock);
		            	stockPriceDao.save(sp);
		            	sp=null;
		            }
	            }catch(Exception e){
	            	e.printStackTrace();
	            }
			}

	}
	

}

package com.redsource.fundamentals.rest.model;

import java.util.ArrayList;

import com.redsource.fundamentals.hibernate.model.Growth;
import com.redsource.fundamentals.hibernate.model.Metrics;
import com.redsource.fundamentals.hibernate.model.Stock;
import com.redsource.fundamentals.hibernate.model.StockPrice;

public class CompanyInfo {
	private ArrayList<Growth> growthModel;
	private ArrayList<StockPrice> stockPriceModel;
	private Stock stockModel;
	private Metrics stockMetrics;
	public ArrayList<Growth> getGrowthModel() {
		return growthModel;
	}

	public void setGrowthModel(ArrayList<Growth> growthModel) {
		this.growthModel = growthModel;
	}

	public Stock getStockModel() {
		return stockModel;
	}

	public void setStockModel(Stock stockModel) {
		this.stockModel = stockModel;
	}

	public Metrics getStockMetrics() {
		return stockMetrics;
	}

	public void setStockMetrics(Metrics stockMetrics) {
		this.stockMetrics = stockMetrics;
	}

	public ArrayList<StockPrice> getStockPriceModel() {
		return stockPriceModel;
	}

	public void setStockPriceModel(ArrayList<StockPrice> stockPriceModel) {
		this.stockPriceModel = stockPriceModel;
	}
	
	
}

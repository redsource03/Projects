package com.redsource.fundamentals.hibernate.dao;

import java.util.Date;
import java.util.List;

import com.redsource.fundamentals.hibernate.model.StockPrice;

public interface StockPriceDao {
	public void save(StockPrice stockPrice);
	public List<StockPrice> getStockPriceSince(String stock, Date startDate);
	public List<StockPrice> getStockPriceAll(String stock);
}

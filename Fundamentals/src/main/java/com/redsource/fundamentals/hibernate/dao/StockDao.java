package com.redsource.fundamentals.hibernate.dao;


import com.redsource.fundamentals.hibernate.model.Stock;

public interface StockDao {
	public Stock getStockInfo(String stock);
}

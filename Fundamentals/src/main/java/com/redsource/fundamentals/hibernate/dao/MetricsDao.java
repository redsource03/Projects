package com.redsource.fundamentals.hibernate.dao;

import com.redsource.fundamentals.hibernate.model.Metrics;

public interface MetricsDao {
	public Metrics getStockMetrics(String stock,String year);
}

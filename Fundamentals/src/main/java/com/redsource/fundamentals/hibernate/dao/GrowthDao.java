package com.redsource.fundamentals.hibernate.dao;

import java.util.ArrayList;

import com.redsource.fundamentals.hibernate.model.Growth;

public interface GrowthDao {
	public ArrayList<Growth> getStockGrowth(String stock);
}

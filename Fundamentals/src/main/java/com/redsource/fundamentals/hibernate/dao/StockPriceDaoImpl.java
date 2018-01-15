package com.redsource.fundamentals.hibernate.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.redsource.fundamentals.hibernate.model.StockPrice;
@Repository
@Transactional
public class StockPriceDaoImpl implements  StockPriceDao{
	private final String GET_STOCK_PRICE_START="FROM StockPrice  where stock=:stock and date>=:date";
	private final String GET_STOCK_PRICE="FROM StockPrice  where stock=:stock order by date asc";
	@PersistenceContext
    private EntityManager entityManager;

	@Override
	public void save(StockPrice stockPrice) {
		// TODO Auto-generated method stub
		entityManager.unwrap(Session.class).save(stockPrice);
	}

	@Override
	public List<StockPrice> getStockPriceSince(String stock, Date startDate) {
		Query query = entityManager.unwrap(Session.class).createQuery(GET_STOCK_PRICE_START);
		query.setParameter("stock", stock);
		query.setParameter("date", startDate);
		return query.list();
		
	}

	@Override
	public List<StockPrice> getStockPriceAll(String stock) {
		Query query = entityManager.unwrap(Session.class).createQuery(GET_STOCK_PRICE);
		query.setParameter("stock", stock);
		return query.list();
	}
	

}

package com.redsource.fundamentals.hibernate.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.redsource.fundamentals.hibernate.model.Stock;
@Repository
@Transactional
public class StockDaoImpl implements StockDao{
	private final String GET_STOCK="FROM Stock  where stock=:stock";
	@PersistenceContext
    private EntityManager entityManager;
	
	@Override
	public Stock getStockInfo(String stock) {
		Query query = entityManager.unwrap(Session.class).createQuery(GET_STOCK);
		query.setParameter("stock", stock);
		if(query.list().size()==1){
			return (Stock) query.list().get(0);
		}else{
			return null;
		}
	}

}

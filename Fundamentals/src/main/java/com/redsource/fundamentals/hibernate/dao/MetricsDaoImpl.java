package com.redsource.fundamentals.hibernate.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.redsource.fundamentals.hibernate.model.Metrics;
@Repository
@Transactional
public class MetricsDaoImpl implements MetricsDao{
	@PersistenceContext
    private EntityManager entityManager;
	private final String GET_STOCK="FROM Metrics  where stock=:stock and year=:year";
	@Override
	public Metrics getStockMetrics(String stock, String year) {
		Query query = entityManager.unwrap(Session.class).createQuery(GET_STOCK);
		query.setParameter("stock", stock);
		query.setParameter("year", year);
		if(query.list().size()==1){
			return (Metrics) query.list().get(0);
		}else{
			return null;
		}
	}

}

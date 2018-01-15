package com.redsource.fundamentals.hibernate.dao;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.redsource.fundamentals.hibernate.model.Growth;


@Repository
@Transactional
public class GrowthDaoImpl implements GrowthDao{
	private final String GET_STOCK="FROM Growth  where stock=:stock order by id asc";
	@PersistenceContext
    private EntityManager entityManager;
	

	@Override
	public ArrayList<Growth> getStockGrowth(String stock) {
		Query query = entityManager.unwrap(Session.class).createQuery(GET_STOCK);
		query.setParameter("stock", stock);
		return new ArrayList<Growth>(query.list());
	}
	

}

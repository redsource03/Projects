package com.redsource.fundamentals.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="growth")
public class Growth {
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
	@Column(name="stock")
	String stock;
	@Column(name="year")
	String year;
	@Column(name="quarter")
	String quarter;
	@Column(name="gross")
	long gross;
	@Column(name="net")
	long net;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getQuarter() {
		return quarter;
	}
	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}
	public long getGross() {
		return gross;
	}
	public void setGross(long gross) {
		this.gross = gross;
	}
	public long getNet() {
		return net;
	}
	public void setNet(long net) {
		this.net = net;
	}
	
	
}

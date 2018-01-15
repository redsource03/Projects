package com.redsource.fundamentals.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="metrics")
public class Metrics {
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
	@Column(name="stock")
	String stock;
	@Column(name="year")
	String year;
	@Column(name="current_r")
	float currentRatio;
	@Column(name="quick_r")
	float quickRatio;
	@Column(name="debt_r")
	float debtRatio;
	@Column(name="debt_equity_r")
	float debtEquityRatio;
	@Column(name="interest_coverage_r")
	float interestCoverageRatio;
	@Column(name="assets_equity_r")
	float assestsEquityRatio;
	@Column(name="gross_profit_margin")
	float grossProfitMargin;
	@Column(name="net_profit_margin")
	float netProfitMargin;
	@Column(name="return_on_assets")
	float returnOnAssests;
	@Column(name="return_on_equity")
	float returnOnEquity;
	@Column(name="price_earning_r")
	float priceEarningRatio;
	@Column(name="eps")
	float earningPerShare;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public float getCurrentRatio() {
		return currentRatio;
	}
	public void setCurrentRatio(float currentRatio) {
		this.currentRatio = currentRatio;
	}
	public float getQuickRatio() {
		return quickRatio;
	}
	public void setQuickRatio(float quickRatio) {
		this.quickRatio = quickRatio;
	}
	public float getDebtRatio() {
		return debtRatio;
	}
	public void setDebtRatio(float debtRatio) {
		this.debtRatio = debtRatio;
	}
	public float getDebtEquityRatio() {
		return debtEquityRatio;
	}
	public void setDebtEquityRatio(float debtEquityRatio) {
		this.debtEquityRatio = debtEquityRatio;
	}
	public float getInterestCoverageRatio() {
		return interestCoverageRatio;
	}
	public void setInterestCoverageRatio(float interestCoverageRatio) {
		this.interestCoverageRatio = interestCoverageRatio;
	}
	public float getAssestsEquityRatio() {
		return assestsEquityRatio;
	}
	public void setAssestsEquityRatio(float assestsEquityRatio) {
		this.assestsEquityRatio = assestsEquityRatio;
	}
	public float getGrossProfitMargin() {
		return grossProfitMargin;
	}
	public void setGrossProfitMargin(float grossProfitMargin) {
		this.grossProfitMargin = grossProfitMargin;
	}
	public float getNetProfitMargin() {
		return netProfitMargin;
	}
	public void setNetProfitMargin(float netProfitMargin) {
		this.netProfitMargin = netProfitMargin;
	}
	public float getReturnOnAssests() {
		return returnOnAssests;
	}
	public void setReturnOnAssests(float returnOnAssests) {
		this.returnOnAssests = returnOnAssests;
	}
	public float getReturnOnEquity() {
		return returnOnEquity;
	}
	public void setReturnOnEquity(float returnOnEquity) {
		this.returnOnEquity = returnOnEquity;
	}
	public float getPriceEarningRatio() {
		return priceEarningRatio;
	}
	public void setPriceEarningRatio(float priceEarningRatio) {
		this.priceEarningRatio = priceEarningRatio;
	}
	public float getEarningPerShare() {
		return earningPerShare;
	}
	public void setEarningPerShare(float earningPerShare) {
		this.earningPerShare = earningPerShare;
	}
	
	
	

}

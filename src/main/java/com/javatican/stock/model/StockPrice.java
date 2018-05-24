package com.javatican.stock.model;

import java.util.Date;

public class StockPrice {

	public StockPrice() {
		super();
	}

	private Date tradingDate;
	private Double tradeVolume;
	private Double tradeValue;
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Double transaction;

	public StockPrice(Date tradingDate, Double tradeVolume, Double tradeValue, Double open, Double high, Double low,
			Double close, Double transaction) {
		super();
		this.tradingDate = tradingDate;
		this.tradeVolume = tradeVolume;
		this.tradeValue = tradeValue;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.transaction = transaction;
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public Double getTradeVolume() {
		return tradeVolume;
	}

	public void setTradeVolume(Double tradeVolume) {
		this.tradeVolume = tradeVolume;
	}

	public Double getTradeValue() {
		return tradeValue;
	}

	public void setTradeValue(Double tradeValue) {
		this.tradeValue = tradeValue;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getTransaction() {
		return transaction;
	}

	public void setTransaction(Double transaction) {
		this.transaction = transaction;
	}
	
}

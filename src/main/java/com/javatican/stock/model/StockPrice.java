package com.javatican.stock.model;

import java.util.Date;

import com.javatican.stock.util.StockUtils;

/*
 * Individual stock daily trading value/volume/price: 
 * http://www.tse.com.tw/en/exchangeReport/STOCK_DAY?response=html&date=20180501&stockNo=2454
 */
public class StockPrice implements Comparable<StockPrice> {

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
	/*
	 * number of transactions
	 */
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

	@Override
	public String toString() {
		return "tradingDate=" + this.getTradingDate() + ", close=" + this.getClose();
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public void setTradingDateAsString(String date) {
		this.tradingDate = StockUtils.stringToDate(date).get();
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

	/*
	 * (non-Javadoc) define natural order by the tradingDate field ascending.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StockPrice o) {
		return this.getTradingDate().compareTo(o.getTradingDate());
	}

}

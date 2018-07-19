package com.javatican.stock.model;

import java.util.Date;

import com.javatican.stock.util.StockUtils;

public class StockItemWeeklyData implements Comparable<StockItemWeeklyData> {

	public StockItemWeeklyData() {
		super();
	}

	private Date tradingDate;
	private StockPrice stockPrice;
	/*
	 * high: the maximum price of the stock item within the n trading day window
	 * before tradingDate.
	 */
	private Double high;
	/*
	 * high: the minimum price of the stock item within the n trading day window
	 * before tradingDate.
	 */
	private Double low;
	private Double rsv;
	private Double k;
	private Double d;
	private Double sma4;
	private Double sma8;
	private Double sma12;
	private Double sma24;

	public StockItemWeeklyData(Date tradingDate, StockPrice stockPrice) {
		super();
		this.tradingDate = tradingDate;
		this.stockPrice = stockPrice;
	}

	public Double getSma4() {
		return sma4;
	}

	public void setSma4(Double sma4) {
		this.sma4 = sma4;
	}

	public Double getSma8() {
		return sma8;
	}

	public void setSma8(Double sma8) {
		this.sma8 = sma8;
	}

	public Double getSma12() {
		return sma12;
	}

	public void setSma12(Double sma12) {
		this.sma12 = sma12;
	}

	public Double getSma24() {
		return sma24;
	}

	public void setSma24(Double sma24) {
		this.sma24 = sma24;
	}

	@Override
	public String toString() {
		return "tradingDate=" + this.getTradingDate() + ", K=" + this.getK();
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

	public StockPrice getStockPrice() {
		return stockPrice;
	}

	public void setStockPrice(StockPrice stockPrice) {
		this.stockPrice = stockPrice;
	}

	public Double getRsv() {
		return rsv;
	}

	public void setRsv(Double rsv) {
		this.rsv = rsv;
	}

	public Double getK() {
		return k;
	}

	public void setK(Double k) {
		this.k = k;
	}

	public Double getD() {
		return d;
	}

	public void setD(Double d) {
		this.d = d;
	}

	/*
	 * (non-Javadoc) define natural order by the tradingDate field ascending.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StockItemWeeklyData o) {
		return this.getTradingDate().compareTo(o.getTradingDate());
	}

}

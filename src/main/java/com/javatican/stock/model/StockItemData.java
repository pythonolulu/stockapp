package com.javatican.stock.model;

import java.util.Date;

import com.javatican.stock.util.StockUtils;

public class StockItemData implements Comparable<StockItemData> {

	public StockItemData() {
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
	private Double sma5;
	private Double sma10;
	private Double sma20;
	private Double sma60;

	public StockItemData(Date tradingDate, StockPrice stockPrice) {
		super();
		this.tradingDate = tradingDate;
		this.stockPrice = stockPrice;
	}

	public Double getSma5() {
		return sma5;
	}

	public void setSma5(Double sma5) {
		this.sma5 = sma5;
	}

	public Double getSma10() {
		return sma10;
	}

	public void setSma10(Double sma10) {
		this.sma10 = sma10;
	}

	public Double getSma20() {
		return sma20;
	}

	public void setSma20(Double sma20) {
		this.sma20 = sma20;
	}

	public Double getSma60() {
		return sma60;
	}

	public void setSma60(Double sma60) {
		this.sma60 = sma60;
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
	public int compareTo(StockItemData o) {
		return this.getTradingDate().compareTo(o.getTradingDate());
	}

}

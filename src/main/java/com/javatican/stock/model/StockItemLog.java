package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "stock_item_log")
public class StockItemLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_item_id", nullable = false)
	private StockItem stockItem;

	@Column(name = "symbol", nullable = false, unique = true)
	private String symbol;

	/*
	 * chart_date: the date of the chart draw for the stock item
	 */
	@Column(name = "chart_date", nullable = true)
	private Date chartDate;
	/*
	 * price_date: the date of the latest record in price data for the stock item
	 */
	@Column(name = "price_date", nullable = true)
	private Date priceDate;

	/*
	 * stats_date: the date of the latest record in calculated stats data(such as
	 * KD, SMA) for the stock item
	 */
	@Column(name = "stats_date", nullable = true)
	private Date statsDate;

	@Column(name = "valid", nullable = true)
	private boolean valid = true;

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Long getId() {
		return id;
	}

	public Date getChartDate() {
		return chartDate;
	}

	public boolean isValid() {
		return valid;
	}

	public void setChartDate(Date chartDate) {
		this.chartDate = chartDate;
	}

	public Date getPriceDate() {
		return priceDate;
	}

	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}

	public Date getStatsDate() {
		return statsDate;
	}

	public void setStatsDate(Date statsDate) {
		this.statsDate = statsDate;
	}

}

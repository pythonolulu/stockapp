package com.javatican.stock.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedAttributeNode;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/* URL for download stock profile 
 * 1. http://mops.twse.com.tw/mops/web/t05st03 GET 
 * 		need to get the cookie: jcsession
 * 2. http://mops.twse.com.tw/mops/web/ajax_t05st03 POST
 * 		need to set the cookie jcsession from 1st step
		and send the form data:
			encodeURIComponent: 1
			step: 1
			firstin: 1
			off: 1
			keyword4: 
			code1: 
			TYPEK2: 
			checkbtn: 
			queryName: co_id
			inpuType: co_id
			TYPEK: all
			co_id: 2317
 */

@Entity
@Table(name = "stock_item")
@NamedEntityGraphs({ @NamedEntityGraph(name = "StockItem.stbt", attributeNodes = @NamedAttributeNode("stbt")),
		@NamedEntityGraph(name = "StockItem.cwts", attributeNodes = @NamedAttributeNode("cwts")),
		@NamedEntityGraph(name = "StockItem.pwts", attributeNodes = @NamedAttributeNode("pwts")) })
public class StockItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "symbol", nullable = false, unique = true)
	private String symbol;

	@Column(name = "name", nullable = true)
	private String name;

	@Column(name = "category", nullable = true)
	private String category;

	@Column(name = "capital", nullable = true)
	private Double capital = 0.0;

	/*
	 * price: the average price (calculated from price data from last trading
	 * month). It is mainly used for some calculations that require approximate
	 * price information.
	 */
	@Column(name = "price", nullable = true)
	private Double price = 0.0;

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

	public Date getChartDate() {
		return chartDate;
	}

	public void setChartDate(Date chartDate) {
		this.chartDate = chartDate;
	}

	/*
	 * chart_date: the date of the chart draw for the stock item
	 */
	@Column(name = "chart_date", nullable = true)
	private Date chartDate;

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

	@JsonIgnore
	@OneToMany(mappedBy = "stockItem")
	private Collection<StockTradeByTrust> stbt;

	@JsonIgnore
	@OneToMany(mappedBy = "stockItem")
	private Collection<CallWarrantTradeSummary> cwts;

	@JsonIgnore
	@OneToMany(mappedBy = "stockItem")
	private Collection<PutWarrantTradeSummary> pwts;

	public StockItem() {
		super();
	}

	public StockItem(String symbol, String name, String category, Double capital, Double price) {
		super();
		this.symbol = symbol;
		this.name = name;
		this.category = category;
		this.capital = capital;
		this.price = price;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getCapital() {
		return capital;
	}

	public void setCapital(Double capital) {
		this.capital = capital;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public Collection<StockTradeByTrust> getStbt() {
		return stbt;
	}
	public Collection<CallWarrantTradeSummary> getCwts() {
		return cwts;
	}

	public Collection<PutWarrantTradeSummary> getPwts() {
		return pwts;
	}



}

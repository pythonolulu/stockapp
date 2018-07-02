package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/*
 * http://www.tse.com.tw/fund/TWT44U?response=html&date=20180525
 */
@Entity
@Table(name = "stock_trade_by_trust", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "trading_date", "stock_symbol" }) })
@NamedEntityGraphs({
		@NamedEntityGraph(name = "StockTradeByTrust.stockItem", attributeNodes = @NamedAttributeNode("stockItem")),
		@NamedEntityGraph(name = "StockTradeByTrust.stockItem.stbt", attributeNodes = @NamedAttributeNode(value = "stockItem", subgraph = "stockItem.stbt"), subgraphs = @NamedSubgraph(name = "stockItem.stbt", attributeNodes = @NamedAttributeNode(value = "stbt"))) })
public class StockTradeByTrust {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private Date tradingDate;

	@ManyToOne(fetch=FetchType.LAZY)
	private StockItem stockItem;

	@Column(name = "stock_symbol", nullable = false)
	private String stockSymbol;

	@Column(name = "buy", nullable = false)
	private Double buy = 0.0;

	@Column(name = "sell", nullable = false)
	private Double sell = 0.0;

	@Column(name = "diff", nullable = false)
	private Double diff = 0.0;

	public StockTradeByTrust() {
		super();
	}

	public StockTradeByTrust(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public StockTradeByTrust(Date tradingDate, String stockSymbol, Double buy, Double sell, Double diff) {
		super();
		this.tradingDate = tradingDate;
		this.stockSymbol = stockSymbol;
		this.buy = buy;
		this.sell = sell;
		this.diff = diff;
	}

	@Override
	public String toString() {
		return String.format("Symbol:%s, buy:%f, sell:%f", this.stockSymbol, this.buy, this.sell);
	}


	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}

	public Double getBuy() {
		return buy;
	}

	public void setBuy(Double buy) {
		this.buy = buy;
	}

	public Double getSell() {
		return sell;
	}

	public void setSell(Double sell) {
		this.sell = sell;
	}

	public Double getDiff() {
		return diff;
	}

	public void setDiff(Double diff) {
		this.diff = diff;
	}

	public Long getId() {
		return id;
	}
}

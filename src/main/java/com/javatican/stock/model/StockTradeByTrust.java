package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "stock_trade_by_trust")
public class StockTradeByTrust {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private Date tradingDate;

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

	public StockTradeByTrust(Date tradingDate, String stockSymbol, Double buy, Double sell, Double diff) {
		super();
		this.tradingDate = tradingDate;
		this.stockSymbol = stockSymbol;
		this.buy = buy;
		this.sell = sell;
		this.diff = diff;
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
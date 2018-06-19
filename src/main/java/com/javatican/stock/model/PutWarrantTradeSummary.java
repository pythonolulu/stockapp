package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne; 
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
/*
 * http://www.tse.com.tw/exchangeReport/MI_INDEX?response=html&date=20180615&type=0999P 
 */
@Entity
@Table(name = "put_warrant_trade_summary", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "trading_date", "stock_symbol" }) })
public class PutWarrantTradeSummary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private Date tradingDate;

	@Column(name = "trade_value", nullable = false)
	private Double tradeValue = 0.0;

	@Column(name = "avg_transaction_value", nullable = false)
	private Double avgTransactionValue = 0.0;

	@Column(name = "transaction", nullable = false)
	private Integer transaction = 0;

	@ManyToOne
	private StockItem stockItem;

	@Column(name = "stock_symbol", nullable = false)
	private String stockSymbol;

	public PutWarrantTradeSummary() {
		super();
	}

	public PutWarrantTradeSummary(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public PutWarrantTradeSummary(Date tradingDate, String stockSymbol) {
		super();
		this.tradingDate = tradingDate;
		this.stockSymbol = stockSymbol;
	}

	@Override
	public String toString() {
		return String.format("Symbol:%s, TradingDate: %tF, Trade value: %f", this.stockSymbol, this.tradingDate, this.tradeValue);
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

	public Long getId() {
		return id;
	}

	public Double getTradeValue() {
		return tradeValue;
	}

	public void setTradeValue(Double tradeValue) {
		this.tradeValue = tradeValue;
	}

	public Integer getTransaction() {
		return transaction;
	}

	public void setTransaction(Integer transaction) {
		this.transaction = transaction;
	}

	public Double getAvgTransactionValue() {
		return avgTransactionValue;
	}

	public void setAvgTransactionValue(Double avgTransactionValue) {
		this.avgTransactionValue = avgTransactionValue;
	}

}

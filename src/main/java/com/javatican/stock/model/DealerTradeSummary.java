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
 *  http://www.tse.com.tw/fund/TWT43U?response=html&date=20180703
 */
@Entity
@Table(name = "dealer_trade_summary", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "trading_date", "stock_symbol" }) })
public class DealerTradeSummary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private Date tradingDate;

	@Column(name = "hedge_buy", nullable = false)
	private Double hedgeBuy = 0.0;

	@Column(name = "hedge_sell", nullable = false)
	private Double hedgeSell = 0.0;

	@Column(name = "hedge_net", nullable = false)
	private Double hedgeNet = 0.0;

	@Column(name = "hedge_call_buy", nullable = false)
	private Double hedgeCallBuy = 0.0;

	@Column(name = "hedge_call_sell", nullable = false)
	private Double hedgeCallSell = 0.0;

	@Column(name = "hedge_call_net", nullable = false)
	private Double hedgeCallNet = 0.0;
	
	@Column(name = "hedge_put_buy", nullable = false)
	private Double hedgePutBuy = 0.0;

	@Column(name = "hedge_put_sell", nullable = false)
	private Double hedgePutSell = 0.0;

	@Column(name = "hedge_put_net", nullable = false)
	private Double hedgePutNet = 0.0;

	@ManyToOne
	private StockItem stockItem;

	@Column(name = "stock_symbol", nullable = false)
	private String stockSymbol;

	public DealerTradeSummary() {
		super();
	}

	public DealerTradeSummary(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public DealerTradeSummary(Date tradingDate, String stockSymbol) {
		super();
		this.tradingDate = tradingDate;
		this.stockSymbol = stockSymbol;
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

	public Double getHedgeBuy() {
		return hedgeBuy;
	}

	public void setHedgeBuy(Double hedgeBuy) {
		this.hedgeBuy = hedgeBuy;
	}

	public Double getHedgeSell() {
		return hedgeSell;
	}

	public void setHedgeSell(Double hedgeSell) {
		this.hedgeSell = hedgeSell;
	}

	public Double getHedgeNet() {
		return hedgeNet;
	}

	public void setHedgeNet(Double hedgeNet) {
		this.hedgeNet = hedgeNet;
	}

	public Double getHedgeCallBuy() {
		return hedgeCallBuy;
	}

	public void setHedgeCallBuy(Double hedgeCallBuy) {
		this.hedgeCallBuy = hedgeCallBuy;
	}

	public Double getHedgeCallSell() {
		return hedgeCallSell;
	}

	public void setHedgeCallSell(Double hedgeCallSell) {
		this.hedgeCallSell = hedgeCallSell;
	}

	public Double getHedgeCallNet() {
		return hedgeCallNet;
	}

	public void setHedgeCallNet(Double hedgeCallNet) {
		this.hedgeCallNet = hedgeCallNet;
	}

	public Double getHedgePutBuy() {
		return hedgePutBuy;
	}

	public void setHedgePutBuy(Double hedgePutBuy) {
		this.hedgePutBuy = hedgePutBuy;
	}

	public Double getHedgePutSell() {
		return hedgePutSell;
	}

	public void setHedgePutSell(Double hedgePutSell) {
		this.hedgePutSell = hedgePutSell;
	}

	public Double getHedgePutNet() {
		return hedgePutNet;
	}

	public void setHedgePutNet(Double hedgePutNet) {
		this.hedgePutNet = hedgePutNet;
	}


}

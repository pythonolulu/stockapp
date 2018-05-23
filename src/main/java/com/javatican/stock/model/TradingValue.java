package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

/*
 * 1. DailyTrading : 
 * http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=20180501
 * 2. TradingValueForeignAndOtherInvestors : 
 * http://www.tse.com.tw/en/fund/BFI82U?response=html&dayDate=20180518&type=day
 * 
 */
@Entity
@Table(name = "trading_value")
public class TradingValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false, unique = true)
	private Date tradingDate;

	@Column(name = "total_value", nullable = false)
	private Double totalValue = 0.0;

	@Column(name = "dealer_buy", nullable = false)
	private Double dealerBuy = 0.0;
	@Column(name = "dealer_sell", nullable = false)
	private Double dealerSell = 0.0;
	@Column(name = "dealer_diff", nullable = false)
	private Double dealerDiff = 0.0;

	@Column(name = "dealer_hedge_buy", nullable = false)
	private Double dealerHedgeBuy = 0.0;
	@Column(name = "dealer_hedge_sell", nullable = false)
	private Double dealerHedgeSell = 0.0;
	@Column(name = "dealer_hedge_diff", nullable = false)
	private Double dealerHedgeDiff = 0.0;

	@Column(name = "trust_buy", nullable = false)
	private Double trustBuy = 0.0;
	@Column(name = "trust_sell", nullable = false)
	private Double trustSell = 0.0;
	@Column(name = "trust_diff", nullable = false)
	private Double trustDiff = 0.0;

	@Column(name = "foreign_buy", nullable = false)
	private Double foreignBuy = 0.0;
	@Column(name = "foreign_sell", nullable = false)
	private Double foreignSell = 0.0;
	@Column(name = "foreign_diff", nullable = false)
	private Double foreignDiff = 0.0;

	public TradingValue() {
		super();
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public Double getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(Double totalValue) {
		this.totalValue = totalValue;
	}

	public Double getDealerBuy() {
		return dealerBuy;
	}

	public void setDealerBuy(Double dealerBuy) {
		this.dealerBuy = dealerBuy;
	}

	public Double getDealerSell() {
		return dealerSell;
	}

	public void setDealerSell(Double dealerSell) {
		this.dealerSell = dealerSell;
	}

	public Double getDealerDiff() {
		return dealerDiff;
	}

	public void setDealerDiff(Double dealerDiff) {
		this.dealerDiff = dealerDiff;
	}

	public Double getDealerHedgeBuy() {
		return dealerHedgeBuy;
	}

	public void setDealerHedgeBuy(Double dealerHedgeBuy) {
		this.dealerHedgeBuy = dealerHedgeBuy;
	}

	public Double getDealerHedgeSell() {
		return dealerHedgeSell;
	}

	public void setDealerHedgeSell(Double dealerHedgeSell) {
		this.dealerHedgeSell = dealerHedgeSell;
	}

	public Double getDealerHedgeDiff() {
		return dealerHedgeDiff;
	}

	public void setDealerHedgeDiff(Double dealerHedgeDiff) {
		this.dealerHedgeDiff = dealerHedgeDiff;
	}

	public Double getTrustBuy() {
		return trustBuy;
	}

	public void setTrustBuy(Double trustBuy) {
		this.trustBuy = trustBuy;
	}

	public Double getTrustSell() {
		return trustSell;
	}

	public void setTrustSell(Double trustSell) {
		this.trustSell = trustSell;
	}

	public Double getTrustDiff() {
		return trustDiff;
	}

	public void setTrustDiff(Double trustDiff) {
		this.trustDiff = trustDiff;
	}

	public Double getForeignBuy() {
		return foreignBuy;
	}

	public void setForeignBuy(Double foreignBuy) {
		this.foreignBuy = foreignBuy;
	}

	public Double getForeignSell() {
		return foreignSell;
	}

	public void setForeignSell(Double foreignSell) {
		this.foreignSell = foreignSell;
	}

	public Double getForeignDiff() {
		return foreignDiff;
	}

	public void setForeignDiff(Double foreignDiff) {
		this.foreignDiff = foreignDiff;
	}

	public Long getId() {
		return id;
	}
}

package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

/*
 * 1. DailyTrading : 
 * http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=20180501
 * 2. TradingValueForeignAndOtherInvestors : 
 * http://www.tse.com.tw/en/fund/BFI82U?response=html&dayDate=20180518&type=day
 * 
 * Note: the corresponding table name was renamed to 'trading_data' , at some stages, 
 * MySQL import tool has problems dealing with table name with 'value' word in it.
 */
@Entity
@Table(name = "trading_data")
public class TradingValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false, unique = true)
	private Date tradingDate;
	@Column(name = "open", nullable = false)
	private Double open;
	@Column(name = "high", nullable = false)
	private Double high;
	@Column(name = "low", nullable = false)
	private Double low;
	@Column(name = "close", nullable = false)
	private Double close;

	@Column(name = "max9", nullable = true)
	private Double max9;
	@Column(name = "min9", nullable = true)
	private Double min9;
	@Column(name = "rsv", nullable = true)
	private Double rsv;
	@Column(name = "k", nullable = true)
	private Double k;
	@Column(name = "d", nullable = true)
	private Double d;
	@Column(name = "sma5", nullable = true)
	private Double sma5;
	@Column(name = "sma10", nullable = true)
	private Double sma10;
	@Column(name = "sma20", nullable = true)
	private Double sma20;
	@Column(name = "sma60", nullable = true)
	private Double sma60;

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

	@Column(name = "margin_buy", nullable = true)
	private Double marginBuy = 0.0;
	@Column(name = "margin_redemp", nullable = true)
	private Double marginRedemp = 0.0;
	@Column(name = "margin_acc", nullable = true)
	private Double marginAcc = 0.0;

	@Column(name = "margin_buy_value", nullable = true)
	private Double marginBuyValue = 0.0;
	@Column(name = "margin_redemp_value", nullable = true)
	private Double marginRedempValue = 0.0;
	@Column(name = "margin_acc_value", nullable = true)
	private Double marginAccValue = 0.0;

	@Column(name = "short_redemp", nullable = true)
	private Double shortRedemp = 0.0;
	@Column(name = "short_sell", nullable = true)
	private Double shortSell = 0.0;
	@Column(name = "short_acc", nullable = true)
	private Double shortAcc = 0.0;

	public TradingValue() {
		super();
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
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

	public Double getMax9() {
		return max9;
	}

	public void setMax9(Double max9) {
		this.max9 = max9;
	}

	public Double getMin9() {
		return min9;
	}

	public void setMin9(Double min9) {
		this.min9 = min9;
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

	public Double getMarginBuy() {
		return marginBuy;
	}

	public void setMarginBuy(Double marginBuy) {
		this.marginBuy = marginBuy;
	}

	public Double getMarginRedemp() {
		return marginRedemp;
	}

	public void setMarginRedemp(Double marginRedemp) {
		this.marginRedemp = marginRedemp;
	}

	public Double getMarginAcc() {
		return marginAcc;
	}

	public void setMarginAcc(Double marginAcc) {
		this.marginAcc = marginAcc;
	}

	public Double getMarginBuyValue() {
		return marginBuyValue;
	}

	public void setMarginBuyValue(Double marginBuyValue) {
		this.marginBuyValue = marginBuyValue;
	}

	public Double getMarginRedempValue() {
		return marginRedempValue;
	}

	public void setMarginRedempValue(Double marginRedempValue) {
		this.marginRedempValue = marginRedempValue;
	}

	public Double getMarginAccValue() {
		return marginAccValue;
	}

	public void setMarginAccValue(Double marginAccValue) {
		this.marginAccValue = marginAccValue;
	}

	public Double getShortRedemp() {
		return shortRedemp;
	}

	public void setShortRedemp(Double shortRedemp) {
		this.shortRedemp = shortRedemp;
	}

	public Double getShortSell() {
		return shortSell;
	}

	public void setShortSell(Double shortSell) {
		this.shortSell = shortSell;
	}

	public Double getShortAcc() {
		return shortAcc;
	}

	public void setShortAcc(Double shortAcc) {
		this.shortAcc = shortAcc;
	}
}

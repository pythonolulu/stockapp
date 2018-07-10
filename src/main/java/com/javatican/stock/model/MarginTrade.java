package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;
 
@Entity
@Table(name = "margin_trade")
public class MarginTrade {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false, unique = true)
	private Date tradingDate;

	@Column(name = "margin_buy", nullable = false)
	private Double marginBuy = 0.0;
	@Column(name = "margin_redemp", nullable = false)
	private Double marginRedemp = 0.0;
	@Column(name = "margin_acc", nullable = false)
	private Double marginAcc = 0.0;  

	@Column(name = "margin_buy_value", nullable = false)
	private Double marginBuyValue = 0.0;
	@Column(name = "margin_redemp_value", nullable = false)
	private Double marginRedempValue = 0.0;
	@Column(name = "margin_acc_value", nullable = false)
	private Double marginAccValue = 0.0;  


	@Column(name = "short_redemp", nullable = false)
	private Double shortRedemp = 0.0;
	@Column(name = "short_sell", nullable = false)
	private Double shortSell = 0.0;
	@Column(name = "short_acc", nullable = false)
	private Double shortAcc = 0.0;  
	
	public MarginTrade() {
		super();
	}

	public MarginTrade(Date tradingDate) {
		super();
		this.tradingDate=tradingDate;
	}
	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
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

	public Long getId() {
		return id;
	}
}

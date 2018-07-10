package com.javatican.stock.model;

import java.util.Date;

public class MarginSblWithDate extends MarginSbl {
	private Date tradingDate;
	
	public MarginSblWithDate() {
		// TODO Auto-generated constructor stub
	}

	public MarginSblWithDate(Date tradingDate) {
		super();
		this.tradingDate=tradingDate;
	}
	public MarginSblWithDate(Date tradingDate, MarginSblWithSymbol msws) {
		super();
		this.tradingDate=tradingDate;
		this.setBuy(msws.getBuy());
		this.setBuyRedemp(msws.getBuyRedemp());
		this.setBuyAcc(msws.getBuyAcc());
		this.setShortSell(msws.getShortSell());
		this.setShortRedemp(msws.getShortRedemp());
		this.setShortAcc(msws.getShortAcc());
		this.setSbl(msws.getSbl());
		this.setSblRedemp(msws.getSblRedemp());
		this.setSblAcc(msws.getSblAcc());
		this.setLimit(msws.getLimit());
	}
	
	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

}

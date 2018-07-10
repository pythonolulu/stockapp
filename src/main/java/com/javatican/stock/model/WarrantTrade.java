package com.javatican.stock.model;

public class WarrantTrade {

	private String warrantSymbol;

	private Double avgPrice = 0.0;

	private Double tradeValue = 0.0;

	private String stockSymbol;

	public WarrantTrade() {
		super();
	}

	public WarrantTrade(String warrantSymbol, Double avgPrice, Double tradeValue, String stockSymbol) {
		super();
		this.warrantSymbol = warrantSymbol;
		this.avgPrice = avgPrice;
		this.tradeValue=tradeValue;
		this.stockSymbol = stockSymbol;
	}

	public Double getTradeValue() {
		return tradeValue;
	}

	public void setTradeValue(Double tradeValue) {
		this.tradeValue = tradeValue;
	}

	public String getWarrantSymbol() {
		return warrantSymbol;
	}

	public void setWarrantSymbol(String warrantSymbol) {
		this.warrantSymbol = warrantSymbol;
	}

	public Double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(Double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}

}

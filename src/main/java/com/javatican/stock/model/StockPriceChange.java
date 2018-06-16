package com.javatican.stock.model;

public class StockPriceChange extends StockPrice {
	private String symbol;
	private String name;
	private Double change;
	private Double changePercent;
	private StockItem stockItem;


	@Override
	public String toString() {
		return "Symbol=" + this.getSymbol() + ", name=" + this.getName() + ", tradingDate=" + this.getTradingDate()
				+ ", close=" + this.getClose() + ", change=" + this.getChange();
	}

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getChange() {
		return change;
	}

	public Double getChangePercent() {
		return changePercent;
	}

	public void setChangePercent(Double changePercent) {
		this.changePercent = changePercent;
	}

	public void setChange(Double change) {
		this.change = change;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public StockPriceChange() {
		super();
	}

}

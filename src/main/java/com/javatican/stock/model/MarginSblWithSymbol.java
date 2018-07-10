package com.javatican.stock.model;

public class MarginSblWithSymbol extends MarginSbl {
	private String symbol;

	public MarginSblWithSymbol() {
		// TODO Auto-generated constructor stub
	}

	public MarginSblWithSymbol(String symbol) {
		super();
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}

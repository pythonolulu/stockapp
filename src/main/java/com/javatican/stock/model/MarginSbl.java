package com.javatican.stock.model;

public abstract class MarginSbl {
	private Double buy=0.0;
	private Double buyRedemp=0.0;
	private Double buyAcc=0.0;
	private Double shortSell=0.0;
	private Double shortRedemp=0.0;
	private Double shortAcc=0.0;
	private Double limit=0.0; 
	private Double sbl=0.0;
	private Double sblRedemp=0.0;
	private Double sblAcc=0.0;
	public MarginSbl() {
		super();
	}


	public Double getBuy() {
		return buy;
	}

	public void setBuy(Double buy) {
		this.buy = buy;
	}

	public Double getBuyRedemp() {
		return buyRedemp;
	}

	public void setBuyRedemp(Double buyRedemp) {
		this.buyRedemp = buyRedemp;
	}

	public Double getBuyAcc() {
		return buyAcc;
	}

	public void setBuyAcc(Double buyAcc) {
		this.buyAcc = buyAcc;
	}

	public Double getShortSell() {
		return shortSell;
	}

	public void setShortSell(Double shortSell) {
		this.shortSell = shortSell;
	}

	public Double getShortRedemp() {
		return shortRedemp;
	}

	public void setShortRedemp(Double shortRedemp) {
		this.shortRedemp = shortRedemp;
	}

	public Double getShortAcc() {
		return shortAcc;
	}

	public void setShortAcc(Double shortAcc) {
		this.shortAcc = shortAcc;
	}

	public Double getLimit() {
		return limit;
	}

	public void setLimit(Double limit) {
		this.limit = limit;
	}

	public Double getSbl() {
		return sbl;
	}

	public void setSbl(Double sbl) {
		this.sbl = sbl;
	}

	public Double getSblRedemp() {
		return sblRedemp;
	}

	public void setSblRedemp(Double sblRedemp) {
		this.sblRedemp = sblRedemp;
	}

	public Double getSblAcc() {
		return sblAcc;
	}

	public void setSblAcc(Double sblAcc) {
		this.sblAcc = sblAcc;
	}

	

}

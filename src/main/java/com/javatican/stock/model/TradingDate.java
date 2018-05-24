package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

import com.javatican.stock.util.StockUtils;
/*
* DailyTrading : 
* http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=20180501
* */
@Entity
@Table(name = "trading_date")
public class TradingDate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date", nullable = false, unique = true)
	private Date date;

	public TradingDate() {
		super();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDateAsString(String date) {
		this.date = StockUtils.stringToDate(date).get();
	}

	public Long getId() {
		return id;
	}

}

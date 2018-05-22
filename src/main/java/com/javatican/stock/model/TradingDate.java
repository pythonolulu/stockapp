package com.javatican.stock.model;
import java.util.Date;

import javax.persistence.*;

import com.javatican.stock.util.StockUtils;

@Entity
@Table(name="trading_date")
public class TradingDate {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="date", nullable=false)
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

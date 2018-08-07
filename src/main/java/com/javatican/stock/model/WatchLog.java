package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "watch_log")
public class WatchLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "symbol", nullable = false)
	private String symbol;

	@Column(name = "log_date", nullable = false)
	private Date logDate;
	
	@Column(name = "content", nullable = true)
	private String content;
    
	@ManyToOne(fetch = FetchType.LAZY)
	private WatchItem watchItem;

	public String getSymbol() {
		return symbol;
	}

	public WatchItem getWatchItem() {
		return watchItem;
	}

	public void setWatchItem(WatchItem watchItem) {
		this.watchItem = watchItem;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


	public Long getId() {
		return id;
	}

	public WatchLog() {
		super();
	}

	public WatchLog(String symbol, Date logDate, String content, WatchItem watchItem) {
		super();
		this.symbol = symbol;
		this.logDate = logDate;
		this.content = content;
		this.watchItem = watchItem;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}
 
}
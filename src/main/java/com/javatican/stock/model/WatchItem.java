package com.javatican.stock.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.javatican.stock.util.StockUtils;

@Entity
@Table(name = "watch_item", uniqueConstraints = { @UniqueConstraint(columnNames = { "symbol", "site_user_id" }) })
@NamedEntityGraphs({ @NamedEntityGraph(name = "WatchItem.wl", attributeNodes = @NamedAttributeNode("wl")),
		@NamedEntityGraph(name = "WatchItem.wl_stockItem", attributeNodes = { @NamedAttributeNode("stockItem"),
				@NamedAttributeNode("wl")})})
public class WatchItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "symbol", nullable = false)
	private String symbol;

	@Column(name = "watch_date", nullable = true)
	private Date watchDate;

	@ManyToOne(fetch = FetchType.LAZY)
	private StockItem stockItem;

	@ManyToOne(fetch = FetchType.LAZY)
	private SiteUser siteUser;

	@JsonIgnore
	@OneToMany(mappedBy = "watchItem", cascade= CascadeType.REMOVE )
	private Collection<WatchLog> wl;

	public WatchItem(StockItem si, Date watchDate, SiteUser siteUser) {
		super();
		this.stockItem = si;
		this.symbol = si.getSymbol();
		this.watchDate = watchDate;
		this.siteUser = siteUser;
	}

	public WatchItem(StockItem si, SiteUser siteUser) {
		this(si, StockUtils.todayWithoutTime(), siteUser);
	}

	public WatchItem() {
		super();
	}

	public Collection<WatchLog> getWl() {
		return wl;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getWatchDate() {
		return watchDate;
	}

	public void setWatchDate(Date watchDate) {
		this.watchDate = watchDate;
	}

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public SiteUser getSiteUser() {
		return siteUser;
	}

	public void setSiteUser(SiteUser siteUser) {
		this.siteUser = siteUser;
	}

	public Long getId() {
		return id;
	}

}
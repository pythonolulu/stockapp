package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

//import com.javatican.stock.util.NullOrNotBlank;

@Entity
@Table(name = "portfolio_item")
@NamedEntityGraph(name = "PortfolioItem.stockItem", attributeNodes = @NamedAttributeNode("stockItem"))
public class PortfolioItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// @NullOrNotBlank(message="{portfolioItem.symbol.message}")
	@NotNull
	@Column(name = "symbol", nullable = false)
	private String symbol;

	@Column(name = "warrant_symbol", nullable = true)
	private String warrantSymbol;

	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "buy_date", nullable = true)
	private Date buyDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "sell_date", nullable = true)
	private Date sellDate;

	@NotNull
	@Column(name = "buy_price", nullable = true)
	private Double buyPrice = null;

	@Column(name = "sell_price", nullable = true)
	private Double sellPrice = null;

	@NotNull
	@Column(name = "buy_quantity", nullable = true)
	private Double buyQuantity = null;

	@Column(name = "sell_quantity", nullable = true)
	private Double sellQuantity = null;

	@Column(name = "buy_value", nullable = true)
	private Double buyValue = null;

	@Column(name = "sell_value", nullable = true)
	private Double sellValue = null;

	@Column(name = "profit", nullable = true)
	private Double profit = null;

	@Column(name = "is_warrant", nullable = true)
	private boolean isWarrant = false;

	@Column(name = "is_short", nullable = true)
	private boolean isShort = false;

	@Column(name = "is_closed", nullable = true)
	private boolean isClosed = false;

	@ManyToOne(fetch = FetchType.LAZY)
	private StockItem stockItem;

	@ManyToOne(fetch = FetchType.LAZY)
	private SiteUser siteUser;

	public SiteUser getSiteUser() {
		return siteUser;
	}

	public void setSiteUser(SiteUser siteUser) {
		this.siteUser = siteUser;
	}

	public PortfolioItem() {
	}

	public PortfolioItem(String symbol, SiteUser su) {
		this.symbol = symbol;
		this.siteUser = su;
	}

	public PortfolioItem(StockItem si, SiteUser su) {
		this.symbol = si.getSymbol();
		this.stockItem = si;
		this.siteUser = su;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(Date buyDate) {
		this.buyDate = buyDate;
	}

	public Date getSellDate() {
		return sellDate;
	}

	public void setSellDate(Date sellDate) {
		this.sellDate = sellDate;
	}

	public Double getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(Double buyPrice) {
		this.buyPrice = buyPrice;
	}

	public Double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(Double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public Double getBuyQuantity() {
		return buyQuantity;
	}

	public void setBuyQuantity(Double buyQuantity) {
		this.buyQuantity = buyQuantity;
	}

	public Double getSellQuantity() {
		return sellQuantity;
	}

	public void setSellQuantity(Double sellQuantity) {
		this.sellQuantity = sellQuantity;
	}

	public Double getProfit() {
		return profit;
	}

	public void setProfit(Double profit) {
		this.profit = profit;
	}

	public boolean getIsWarrant() {
		return isWarrant;
	}

	public void setIsWarrant(boolean isWarrant) {
		this.isWarrant = isWarrant;
	}

	public String getWarrantSymbol() {
		return warrantSymbol;
	}

	public void setWarrantSymbol(String warrantSymbol) {
		this.warrantSymbol = warrantSymbol;
	}

	public boolean getIsShort() {
		return isShort;
	}

	public boolean getIsClosed() {
		return isClosed;
	}

	public void setIsShort(boolean isShort) {
		this.isShort = isShort;
	}

	public void setIsClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getBuyValue() {
		return buyValue;
	}

	public void setBuyValue(Double buyValue) {
		this.buyValue = buyValue;
	}

	public Double getSellValue() {
		return sellValue;
	}

	public void setSellValue(Double sellValue) {
		this.sellValue = sellValue;
	}

}
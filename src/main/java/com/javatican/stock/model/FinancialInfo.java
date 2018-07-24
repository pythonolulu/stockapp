package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/*
 * http://mops.twse.com.tw/server-java/t164sb01
 * POST:
 * Parameters:
 * step: 1
 * DEBUG: 
 * CO_ID: 2330
 * SYEAR: 2017
 * SSEASON: 4
 * REPORT_ID: C
 */
@Entity
@Table(name = "financial_info", uniqueConstraints = { @UniqueConstraint(columnNames = { "audit_date", "symbol" }) })
public class FinancialInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "symbol", nullable = false)
	private String symbol;

	@ManyToOne
	private StockItem stockItem;

	@Column(name = "audit_date", nullable = false)
	private Date auditDate;

	@Column(name = "year")
	private Integer year;

	@Column(name = "season")
	private Integer season;

	@Column(name = "assets_cash")
	private Double assetsCash = 0.0;
	@Column(name = "assets_account_receivable")
	private Double assetsAccountReceivable = 0.0;
	@Column(name = "assets_inventories")
	private Double assetsInventories = 0.0;
	@Column(name = "assets_current")
	private Double assetsCurrent = 0.0;
	@Column(name = "assets_ppe")
	private Double assetsPpe = 0.0;
	@Column(name = "assets_non_current")
	private Double assetsNonCurrent = 0.0;
	@Column(name = "assets_total")
	private Double assetsTotal = 0.0;

	@Column(name = "liabilities_current")
	private Double liabilitiesCurrent = 0.0;
	@Column(name = "liabilities_non_current")
	private Double liabilitiesNonCurrent = 0.0;
	@Column(name = "liabilities_total")
	private Double liabilitiesTotal = 0.0;
	@Column(name = "capital_stock_par_value")
	private Double capitalStockParValue = 0.0;
	@Column(name = "equity_shareholders_parent")
	private Double equityShareholdersParent = 0.0;

	@Column(name = "net_asset_value_per_share")
	private Double netAssetValuePerShare = 0.0;

	@Column(name = "liabilities_and_equity")
	private Double liabilitiesAndEquity = 0.0;

	@Column(name = "net_revenue")
	private Double netRevenue = 0.0;
	@Column(name = "cost_revenue")
	private Double costRevenue = 0.0;
	@Column(name = "gross_profit")
	private Double grossProfit = 0.0;
	@Column(name = "profit_margin")
	private Double profitMargin = 0.0;
	@Column(name = "total_operating_expenses")
	private Double totalOperatingExpenses = 0.0;
	@Column(name = "income_from_operations")
	private Double incomeFromOperations = 0.0;
	@Column(name = "non_operating_income_expenses")
	private Double nonOperatingIncomeExpenses = 0.0;
	@Column(name = "income_before_tax")
	private Double incomeBeforeTax = 0.0;
	@Column(name = "income_tax")
	private Double incomeTax = 0.0;
	@Column(name = "net_income")
	private Double netIncome = 0.0;
	@Column(name = "eps")
	private Double eps = 0.0;

	@Column(name = "net_cash_operating")
	private Double netCashOperating = 0.0;
	@Column(name = "net_cash_investing")
	private Double netCashInvesting = 0.0;
	@Column(name = "net_cash_financing")
	private Double netCashFinancing = 0.0;
	@Column(name = "free_cash_flow")
	private Double freeCashFlow = 0.0;

	@Column(name = "effect_exchange_rate")
	private Double effectExchangeRate = 0.0;
	@Column(name = "net_cash")
	private Double netCash = 0.0;
	@Column(name = "cash_begin")
	private Double cashBegin = 0.0;
	@Column(name = "cash_end")
	private Double cashEnd = 0.0;

	public FinancialInfo() {
		super();
	}

	public FinancialInfo(String symbol, Date auditDate, Integer year, Integer season) {
		super();
		this.symbol = symbol;
		this.auditDate = auditDate;
		this.year = year;
		this.season = season;
	}

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public Double getNetAssetValuePerShare() {
		return netAssetValuePerShare;
	}

	public void setNetAssetValuePerShare(Double netAssetValuePerShare) {
		this.netAssetValuePerShare = netAssetValuePerShare;
	}

	public Date getAuditDate() {
		return auditDate;
	}

	public void setAuditDate(Date auditDate) {
		this.auditDate = auditDate;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getSeason() {
		return season;
	}

	public void setSeason(Integer season) {
		this.season = season;
	}

	public Double getAssetsCash() {
		return assetsCash;
	}

	public void setAssetsCash(Double assetsCash) {
		this.assetsCash = assetsCash;
	}

	public Double getAssetsAccountReceivable() {
		return assetsAccountReceivable;
	}

	public void setAssetsAccountReceivable(Double assetsAccountReceivable) {
		this.assetsAccountReceivable = assetsAccountReceivable;
	}

	public Double getAssetsInventories() {
		return assetsInventories;
	}

	public void setAssetsInventories(Double assetsInventories) {
		this.assetsInventories = assetsInventories;
	}

	public Double getAssetsCurrent() {
		return assetsCurrent;
	}

	public void setAssetsCurrent(Double assetsCurrent) {
		this.assetsCurrent = assetsCurrent;
	}

	public Double getAssetsPpe() {
		return assetsPpe;
	}

	public void setAssetsPpe(Double assetsPpe) {
		this.assetsPpe = assetsPpe;
	}

	public Double getAssetsNonCurrent() {
		return assetsNonCurrent;
	}

	public void setAssetsNonCurrent(Double assetsNonCurrent) {
		this.assetsNonCurrent = assetsNonCurrent;
	}

	public Double getAssetsTotal() {
		return assetsTotal;
	}

	public void setAssetsTotal(Double assetsTotal) {
		this.assetsTotal = assetsTotal;
	}

	public Double getLiabilitiesCurrent() {
		return liabilitiesCurrent;
	}

	public void setLiabilitiesCurrent(Double liabilitiesCurrent) {
		this.liabilitiesCurrent = liabilitiesCurrent;
	}

	public Double getLiabilitiesNonCurrent() {
		return liabilitiesNonCurrent;
	}

	public void setLiabilitiesNonCurrent(Double liabilitiesNonCurrent) {
		this.liabilitiesNonCurrent = liabilitiesNonCurrent;
	}

	public Double getLiabilitiesTotal() {
		return liabilitiesTotal;
	}

	public void setLiabilitiesTotal(Double liabilitiesTotal) {
		this.liabilitiesTotal = liabilitiesTotal;
	}

	public Double getCapitalStockParValue() {
		return capitalStockParValue;
	}

	public void setCapitalStockParValue(Double capitalStockParValue) {
		this.capitalStockParValue = capitalStockParValue;
	}

	public Double getEquityShareholdersParent() {
		return equityShareholdersParent;
	}

	public void setEquityShareholdersParent(Double equityShareholdersParent) {
		this.equityShareholdersParent = equityShareholdersParent;
	}

	public Double getLiabilitiesAndEquity() {
		return liabilitiesAndEquity;
	}

	public void setLiabilitiesAndEquity(Double liabilitiesAndEquity) {
		this.liabilitiesAndEquity = liabilitiesAndEquity;
	}

	public Double getNetRevenue() {
		return netRevenue;
	}

	public void setNetRevenue(Double netRevenue) {
		this.netRevenue = netRevenue;
	}

	public Double getCostRevenue() {
		return costRevenue;
	}

	public void setCostRevenue(Double costRevenue) {
		this.costRevenue = costRevenue;
	}

	public Double getGrossProfit() {
		return grossProfit;
	}

	public void setGrossProfit(Double grossProfit) {
		this.grossProfit = grossProfit;
	}

	public Double getProfitMargin() {
		return profitMargin;
	}

	public void setProfitMargin(Double profitMargin) {
		this.profitMargin = profitMargin;
	}

	public Double getTotalOperatingExpenses() {
		return totalOperatingExpenses;
	}

	public void setTotalOperatingExpenses(Double totalOperatingExpenses) {
		this.totalOperatingExpenses = totalOperatingExpenses;
	}

	public Double getIncomeFromOperations() {
		return incomeFromOperations;
	}

	public void setIncomeFromOperations(Double incomeFromOperations) {
		this.incomeFromOperations = incomeFromOperations;
	}

	public Double getNonOperatingIncomeExpenses() {
		return nonOperatingIncomeExpenses;
	}

	public void setNonOperatingIncomeExpenses(Double nonOperatingIncomeExpenses) {
		this.nonOperatingIncomeExpenses = nonOperatingIncomeExpenses;
	}

	public Double getIncomeBeforeTax() {
		return incomeBeforeTax;
	}

	public void setIncomeBeforeTax(Double incomeBeforeTax) {
		this.incomeBeforeTax = incomeBeforeTax;
	}

	public Double getIncomeTax() {
		return incomeTax;
	}

	public void setIncomeTax(Double incomeTax) {
		this.incomeTax = incomeTax;
	}

	public Double getNetIncome() {
		return netIncome;
	}

	public void setNetIncome(Double netIncome) {
		this.netIncome = netIncome;
	}

	public Double getEps() {
		return eps;
	}

	public void setEps(Double eps) {
		this.eps = eps;
	}

	public Double getNetCashOperating() {
		return netCashOperating;
	}

	public void setNetCashOperating(Double netCashOperating) {
		this.netCashOperating = netCashOperating;
	}

	public Double getNetCashInvesting() {
		return netCashInvesting;
	}

	public void setNetCashInvesting(Double netCashInvesting) {
		this.netCashInvesting = netCashInvesting;
	}

	public Double getNetCashFinancing() {
		return netCashFinancing;
	}

	public void setNetCashFinancing(Double netCashFinancing) {
		this.netCashFinancing = netCashFinancing;
	}

	public Double getFreeCashFlow() {
		return freeCashFlow;
	}

	public void setFreeCashFlow(Double freeCashFlow) {
		this.freeCashFlow = freeCashFlow;
	}

	public Double getEffectExchangeRate() {
		return effectExchangeRate;
	}

	public void setEffectExchangeRate(Double effectExchangeRate) {
		this.effectExchangeRate = effectExchangeRate;
	}

	public Double getNetCash() {
		return netCash;
	}

	public void setNetCash(Double netCash) {
		this.netCash = netCash;
	}

	public Double getCashBegin() {
		return cashBegin;
	}

	public void setCashBegin(Double cashBegin) {
		this.cashBegin = cashBegin;
	}

	public Double getCashEnd() {
		return cashEnd;
	}

	public void setCashEnd(Double cashEnd) {
		this.cashEnd = cashEnd;
	}

	public Long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}

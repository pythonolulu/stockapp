package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "future_data")
public class FutureData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false, unique = true)
	private Date tradingDate;
	// current month
	@Column(name = "open", nullable = true)
	private Double open;
	@Column(name = "high", nullable = true)
	private Double high;
	@Column(name = "low", nullable = true)
	private Double low;
	@Column(name = "close", nullable = true)
	private Double close;

	@Column(name = "volume_regular", nullable = true)
	private Double volumeRegular;
	@Column(name = "volume_after_hour", nullable = true)
	private Double volumeAfterHour;
	@Column(name = "volume_total", nullable = true)
	private Double volumeTotal;
	// only include tx
	@Column(name = "open_interest", nullable = true)
	private Double openInterest;
	// 2 for next month
	@Column(name = "open2", nullable = true)
	private Double open2;
	@Column(name = "high2", nullable = true)
	private Double high2;
	@Column(name = "low2", nullable = true)
	private Double low2;
	@Column(name = "close2", nullable = true)
	private Double close2;

	@Column(name = "volume_regular2", nullable = true)
	private Double volumeRegular2;
	@Column(name = "volume_after_hour2", nullable = true)
	private Double volumeAfterHour2;
	@Column(name = "volume_total2", nullable = true)
	private Double volumeTotal2;
	// only include tx
	@Column(name = "open_interest2", nullable = true)
	private Double openInterest2;
	// dealer
	@Column(name = "dealer_trading_long", nullable = true)
	private Double dealerTradingLong;
	@Column(name = "dealer_trading_short", nullable = true)
	private Double dealerTradingShort;
	@Column(name = "dealer_trading_net", nullable = true)
	private Double dealerTradingNet;

	@Column(name = "dealer_open_long", nullable = true)
	private Double dealerOpenLong;
	@Column(name = "dealer_open_short", nullable = true)
	private Double dealerOpenShort;
	@Column(name = "dealer_open_net", nullable = true)
	private Double dealerOpenNet;
	// trust
	@Column(name = "trust_trading_long", nullable = true)
	private Double trustTradingLong;
	@Column(name = "trust_trading_short", nullable = true)
	private Double trustTradingShort;
	@Column(name = "trust_trading_net", nullable = true)
	private Double trustTradingNet;

	@Column(name = "trust_open_long", nullable = true)
	private Double trustOpenLong;
	@Column(name = "trust_open_short", nullable = true)
	private Double trustOpenShort;
	@Column(name = "trust_open_net", nullable = true)
	private Double trustOpenNet;
	// foreign
	@Column(name = "foreign_trading_long", nullable = true)
	private Double foreignTradingLong;
	@Column(name = "foreign_trading_short", nullable = true)
	private Double foreignTradingShort;
	@Column(name = "foreign_trading_net", nullable = true)
	private Double foreignTradingNet;

	@Column(name = "foreign_open_long", nullable = true)
	private Double foreignOpenLong;
	@Column(name = "foreign_open_short", nullable = true)
	private Double foreignOpenShort;
	@Column(name = "foreign_open_net", nullable = true)
	private Double foreignOpenNet;
	// current month contract
	// top5
	@Column(name = "buy_oi_top5", nullable = true)
	private Double buyOiTop5;
	@Column(name = "buy_ratio_top5", nullable = true)
	private Double buyRatioTop5;
	@Column(name = "sell_oi_top5", nullable = true)
	private Double sellOiTop5;
	@Column(name = "sell_ratio_top5", nullable = true)
	private Double sellRatioTop5;
	// top10
	@Column(name = "buy_oi_top10", nullable = true)
	private Double buyOiTop10;
	@Column(name = "buy_ratio_top10", nullable = true)
	private Double buyRatioTop10;
	@Column(name = "sell_oi_top10", nullable = true)
	private Double sellOiTop10;
	@Column(name = "sell_ratio_top10", nullable = true)
	private Double sellRatioTop10;
	// include tx and mtx/4
	@Column(name = "total_oi", nullable = true)
	private Double totalOi;
	// all contracts
	// top5
	@Column(name = "buy_oi_top5_all", nullable = true)
	private Double buyOiTop5All;
	@Column(name = "buy_ratio_top5_all", nullable = true)
	private Double buyRatioTop5All;
	@Column(name = "sell_oi_top5_all", nullable = true)
	private Double sellOiTop5All;
	@Column(name = "sell_ratio_top5_all", nullable = true)
	private Double sellRatioTop5All;
	// top10
	@Column(name = "buy_oi_top10_all", nullable = true)
	private Double buyOiTop10All;
	@Column(name = "buy_ratio_top10_all", nullable = true)
	private Double buyRatioTop10All;
	@Column(name = "sell_oi_top10_all", nullable = true)
	private Double sellOiTop10All;
	@Column(name = "sell_ratio_top10_all", nullable = true)
	private Double sellRatioTop10All;
	// include tx and mtx/4
	@Column(name = "total_oi_all", nullable = true)
	private Double totalOiAll;

	public Double getBuyOiTop5All() {
		return buyOiTop5All;
	}

	public void setBuyOiTop5All(Double buyOiTop5All) {
		this.buyOiTop5All = buyOiTop5All;
	}

	public Double getBuyRatioTop5All() {
		return buyRatioTop5All;
	}

	public void setBuyRatioTop5All(Double buyRatioTop5All) {
		this.buyRatioTop5All = buyRatioTop5All;
	}

	public Double getSellOiTop5All() {
		return sellOiTop5All;
	}

	public void setSellOiTop5All(Double sellOiTop5All) {
		this.sellOiTop5All = sellOiTop5All;
	}

	public Double getSellRatioTop5All() {
		return sellRatioTop5All;
	}

	public void setSellRatioTop5All(Double sellRatioTop5All) {
		this.sellRatioTop5All = sellRatioTop5All;
	}

	public Double getBuyOiTop10All() {
		return buyOiTop10All;
	}

	public void setBuyOiTop10All(Double buyOiTop10All) {
		this.buyOiTop10All = buyOiTop10All;
	}

	public Double getBuyRatioTop10All() {
		return buyRatioTop10All;
	}

	public void setBuyRatioTop10All(Double buyRatioTop10All) {
		this.buyRatioTop10All = buyRatioTop10All;
	}

	public Double getSellOiTop10All() {
		return sellOiTop10All;
	}

	public void setSellOiTop10All(Double sellOiTop10All) {
		this.sellOiTop10All = sellOiTop10All;
	}

	public Double getSellRatioTop10All() {
		return sellRatioTop10All;
	}

	public void setSellRatioTop10All(Double sellRatioTop10All) {
		this.sellRatioTop10All = sellRatioTop10All;
	}

	public Double getTotalOiAll() {
		return totalOiAll;
	}

	public void setTotalOiAll(Double totalOiAll) {
		this.totalOiAll = totalOiAll;
	}

	public FutureData() {
		super();
	}

	public FutureData(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getVolumeRegular() {
		return volumeRegular;
	}

	public void setVolumeRegular(Double volumeRegular) {
		this.volumeRegular = volumeRegular;
	}

	public Double getVolumeAfterHour() {
		return volumeAfterHour;
	}

	public void setVolumeAfterHour(Double volumeAfterHour) {
		this.volumeAfterHour = volumeAfterHour;
	}

	public Double getVolumeTotal() {
		return volumeTotal;
	}

	public void setVolumeTotal(Double volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	public Double getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(Double openInterest) {
		this.openInterest = openInterest;
	}

	public Double getOpen2() {
		return open2;
	}

	public void setOpen2(Double open2) {
		this.open2 = open2;
	}

	public Double getHigh2() {
		return high2;
	}

	public void setHigh2(Double high2) {
		this.high2 = high2;
	}

	public Double getLow2() {
		return low2;
	}

	public void setLow2(Double low2) {
		this.low2 = low2;
	}

	public Double getClose2() {
		return close2;
	}

	public void setClose2(Double close2) {
		this.close2 = close2;
	}

	public Double getVolumeRegular2() {
		return volumeRegular2;
	}

	public void setVolumeRegular2(Double volumeRegular2) {
		this.volumeRegular2 = volumeRegular2;
	}

	public Double getVolumeAfterHour2() {
		return volumeAfterHour2;
	}

	public void setVolumeAfterHour2(Double volumeAfterHour2) {
		this.volumeAfterHour2 = volumeAfterHour2;
	}

	public Double getVolumeTotal2() {
		return volumeTotal2;
	}

	public void setVolumeTotal2(Double volumeTotal2) {
		this.volumeTotal2 = volumeTotal2;
	}

	public Double getOpenInterest2() {
		return openInterest2;
	}

	public void setOpenInterest2(Double openInterest2) {
		this.openInterest2 = openInterest2;
	}

	public Double getDealerTradingLong() {
		return dealerTradingLong;
	}

	public void setDealerTradingLong(Double dealerTradingLong) {
		this.dealerTradingLong = dealerTradingLong;
	}

	public Double getDealerTradingShort() {
		return dealerTradingShort;
	}

	public void setDealerTradingShort(Double dealerTradingShort) {
		this.dealerTradingShort = dealerTradingShort;
	}

	public Double getDealerTradingNet() {
		return dealerTradingNet;
	}

	public void setDealerTradingNet(Double dealerTradingNet) {
		this.dealerTradingNet = dealerTradingNet;
	}

	public Double getDealerOpenLong() {
		return dealerOpenLong;
	}

	public void setDealerOpenLong(Double dealerOpenLong) {
		this.dealerOpenLong = dealerOpenLong;
	}

	public Double getDealerOpenShort() {
		return dealerOpenShort;
	}

	public void setDealerOpenShort(Double dealerOpenShort) {
		this.dealerOpenShort = dealerOpenShort;
	}

	public Double getDealerOpenNet() {
		return dealerOpenNet;
	}

	public void setDealerOpenNet(Double dealerOpenNet) {
		this.dealerOpenNet = dealerOpenNet;
	}

	public Double getTrustTradingLong() {
		return trustTradingLong;
	}

	public void setTrustTradingLong(Double trustTradingLong) {
		this.trustTradingLong = trustTradingLong;
	}

	public Double getTrustTradingShort() {
		return trustTradingShort;
	}

	public void setTrustTradingShort(Double trustTradingShort) {
		this.trustTradingShort = trustTradingShort;
	}

	public Double getTrustTradingNet() {
		return trustTradingNet;
	}

	public void setTrustTradingNet(Double trustTradingNet) {
		this.trustTradingNet = trustTradingNet;
	}

	public Double getTrustOpenLong() {
		return trustOpenLong;
	}

	public void setTrustOpenLong(Double trustOpenLong) {
		this.trustOpenLong = trustOpenLong;
	}

	public Double getTrustOpenShort() {
		return trustOpenShort;
	}

	public void setTrustOpenShort(Double trustOpenShort) {
		this.trustOpenShort = trustOpenShort;
	}

	public Double getTrustOpenNet() {
		return trustOpenNet;
	}

	public void setTrustOpenNet(Double trustOpenNet) {
		this.trustOpenNet = trustOpenNet;
	}

	public Double getForeignTradingLong() {
		return foreignTradingLong;
	}

	public void setForeignTradingLong(Double foreignTradingLong) {
		this.foreignTradingLong = foreignTradingLong;
	}

	public Double getForeignTradingShort() {
		return foreignTradingShort;
	}

	public void setForeignTradingShort(Double foreignTradingShort) {
		this.foreignTradingShort = foreignTradingShort;
	}

	public Double getForeignTradingNet() {
		return foreignTradingNet;
	}

	public void setForeignTradingNet(Double foreignTradingNet) {
		this.foreignTradingNet = foreignTradingNet;
	}

	public Double getForeignOpenLong() {
		return foreignOpenLong;
	}

	public void setForeignOpenLong(Double foreignOpenLong) {
		this.foreignOpenLong = foreignOpenLong;
	}

	public Double getForeignOpenShort() {
		return foreignOpenShort;
	}

	public void setForeignOpenShort(Double foreignOpenShort) {
		this.foreignOpenShort = foreignOpenShort;
	}

	public Double getForeignOpenNet() {
		return foreignOpenNet;
	}

	public void setForeignOpenNet(Double foreignOpenNet) {
		this.foreignOpenNet = foreignOpenNet;
	}

	public Double getBuyOiTop5() {
		return buyOiTop5;
	}

	public void setBuyOiTop5(Double buyOiTop5) {
		this.buyOiTop5 = buyOiTop5;
	}

	public Double getBuyRatioTop5() {
		return buyRatioTop5;
	}

	public void setBuyRatioTop5(Double buyRatioTop5) {
		this.buyRatioTop5 = buyRatioTop5;
	}

	public Double getSellOiTop5() {
		return sellOiTop5;
	}

	public void setSellOiTop5(Double sellOiTop5) {
		this.sellOiTop5 = sellOiTop5;
	}

	public Double getSellRatioTop5() {
		return sellRatioTop5;
	}

	public void setSellRatioTop5(Double sellRatioTop5) {
		this.sellRatioTop5 = sellRatioTop5;
	}

	public Double getBuyOiTop10() {
		return buyOiTop10;
	}

	public void setBuyOiTop10(Double buyOiTop10) {
		this.buyOiTop10 = buyOiTop10;
	}

	public Double getBuyRatioTop10() {
		return buyRatioTop10;
	}

	public void setBuyRatioTop10(Double buyRatioTop10) {
		this.buyRatioTop10 = buyRatioTop10;
	}

	public Double getSellOiTop10() {
		return sellOiTop10;
	}

	public void setSellOiTop10(Double sellOiTop10) {
		this.sellOiTop10 = sellOiTop10;
	}

	public Double getSellRatioTop10() {
		return sellRatioTop10;
	}

	public void setSellRatioTop10(Double sellRatioTop10) {
		this.sellRatioTop10 = sellRatioTop10;
	}

	public Double getTotalOi() {
		return totalOi;
	}

	public void setTotalOi(Double totalOi) {
		this.totalOi = totalOi;
	}

	public Long getId() {
		return id;
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

}

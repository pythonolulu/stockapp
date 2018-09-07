package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "option_data")
public class OptionData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false, unique = true)
	private Date tradingDate;
	// call option trade volume
	@Column(name = "call_trading_volume", nullable = true)
	private Double callTradingVolume;
	// put option trade volume
	@Column(name = "put_trading_volume", nullable = true)
	private Double putTradingVolume;
	// call option OI : same as callTotalOiAll
	@Column(name = "call_oi", nullable = true)
	private Double callOi;
	// put option OI : same as putTotalOiAll
	@Column(name = "put_oi", nullable = true)
	private Double putOi;
	// dealer call option
	@Column(name = "call_dealer_trading_long", nullable = true)
	private Double callDealerTradingLong;
	@Column(name = "call_dealer_trading_value_long", nullable = true)
	private Double callDealerTradingValueLong;
	@Column(name = "call_dealer_trading_short", nullable = true)
	private Double callDealerTradingShort;
	@Column(name = "call_dealer_trading_value_short", nullable = true)
	private Double callDealerTradingValueShort;
	@Column(name = "call_dealer_trading_net", nullable = true)
	private Double callDealerTradingNet;
	@Column(name = "call_dealer_trading_value_net", nullable = true)
	private Double callDealerTradingValueNet;
	// dealer call option OI
	@Column(name = "call_dealer_open_long", nullable = true)
	private Double callDealerOpenLong;
	@Column(name = "call_dealer_open_value_long", nullable = true)
	private Double callDealerOpenValueLong;
	@Column(name = "call_dealer_open_short", nullable = true)
	private Double callDealerOpenShort;
	@Column(name = "call_dealer_open_value_short", nullable = true)
	private Double callDealerOpenValueShort;
	@Column(name = "call_dealer_open_net", nullable = true)
	private Double callDealerOpenNet;
	@Column(name = "call_dealer_open_value_net", nullable = true)
	private Double callDealerOpenValueNet;
	// trust call option
	@Column(name = "call_trust_trading_long", nullable = true)
	private Double callTrustTradingLong;
	@Column(name = "call_trust_trading_value_long", nullable = true)
	private Double callTrustTradingValueLong;
	@Column(name = "call_trust_trading_short", nullable = true)
	private Double callTrustTradingShort;
	@Column(name = "call_trust_trading_value_short", nullable = true)
	private Double callTrustTradingValueShort;
	@Column(name = "call_trust_trading_net", nullable = true)
	private Double callTrustTradingNet;
	@Column(name = "call_trust_trading_value_net", nullable = true)
	private Double callTrustTradingValueNet;
	// trust call option OI
	@Column(name = "call_trust_open_long", nullable = true)
	private Double callTrustOpenLong;
	@Column(name = "call_trust_open_value_long", nullable = true)
	private Double callTrustOpenValueLong;
	@Column(name = "call_trust_open_short", nullable = true)
	private Double callTrustOpenShort;
	@Column(name = "call_trust_open_value_short", nullable = true)
	private Double callTrustOpenValueShort;
	@Column(name = "call_trust_open_net", nullable = true)
	private Double callTrustOpenNet;
	@Column(name = "call_trust_open_value_net", nullable = true)
	private Double callTrustOpenValueNet;
	// foreign call option
	@Column(name = "call_foreign_trading_long", nullable = true)
	private Double callForeignTradingLong;
	@Column(name = "call_foreign_trading_value_long", nullable = true)
	private Double callForeignTradingValueLong;
	@Column(name = "call_foreign_trading_short", nullable = true)
	private Double callForeignTradingShort;
	@Column(name = "call_foreign_trading_value_short", nullable = true)
	private Double callForeignTradingValueShort;
	@Column(name = "call_foreign_trading_net", nullable = true)
	private Double callForeignTradingNet;
	@Column(name = "call_foreign_trading_value_net", nullable = true)
	private Double callForeignTradingValueNet;
	// foreign call option OI
	@Column(name = "call_foreign_open_long", nullable = true)
	private Double callForeignOpenLong;
	@Column(name = "call_foreign_open_value_long", nullable = true)
	private Double callForeignOpenValueLong;
	@Column(name = "call_foreign_open_short", nullable = true)
	private Double callForeignOpenShort;
	@Column(name = "call_foreign_open_value_short", nullable = true)
	private Double callForeignOpenValueShort;
	@Column(name = "call_foreign_open_net", nullable = true)
	private Double callForeignOpenNet;
	@Column(name = "call_foreign_open_value_net", nullable = true)
	private Double callForeignOpenValueNet;
	// dealer put option
	@Column(name = "put_dealer_trading_long", nullable = true)
	private Double putDealerTradingLong;
	@Column(name = "put_dealer_trading_value_long", nullable = true)
	private Double putDealerTradingValueLong;
	@Column(name = "put_dealer_trading_short", nullable = true)
	private Double putDealerTradingShort;
	@Column(name = "put_dealer_trading_value_short", nullable = true)
	private Double putDealerTradingValueShort;
	@Column(name = "put_dealer_trading_net", nullable = true)
	private Double putDealerTradingNet;
	@Column(name = "put_dealer_trading_value_net", nullable = true)
	private Double putDealerTradingValueNet;
	// dealer put option OI
	@Column(name = "put_dealer_open_long", nullable = true)
	private Double putDealerOpenLong;
	@Column(name = "put_dealer_open_value_long", nullable = true)
	private Double putDealerOpenValueLong;
	@Column(name = "put_dealer_open_short", nullable = true)
	private Double putDealerOpenShort;
	@Column(name = "put_dealer_open_value_short", nullable = true)
	private Double putDealerOpenValueShort;
	@Column(name = "put_dealer_open_net", nullable = true)
	private Double putDealerOpenNet;
	@Column(name = "put_dealer_open_value_net", nullable = true)
	private Double putDealerOpenValueNet;
	// trust put option
	@Column(name = "put_trust_trading_long", nullable = true)
	private Double putTrustTradingLong;
	@Column(name = "put_trust_trading_value_long", nullable = true)
	private Double putTrustTradingValueLong;
	@Column(name = "put_trust_trading_short", nullable = true)
	private Double putTrustTradingShort;
	@Column(name = "put_trust_trading_value_short", nullable = true)
	private Double putTrustTradingValueShort;
	@Column(name = "put_trust_trading_net", nullable = true)
	private Double putTrustTradingNet;
	@Column(name = "put_trust_trading_value_net", nullable = true)
	private Double putTrustTradingValueNet;
	// trust put option OI
	@Column(name = "put_trust_open_long", nullable = true)
	private Double putTrustOpenLong;
	@Column(name = "put_trust_open_value_long", nullable = true)
	private Double putTrustOpenValueLong;
	@Column(name = "put_trust_open_short", nullable = true)
	private Double putTrustOpenShort;
	@Column(name = "put_trust_open_value_short", nullable = true)
	private Double putTrustOpenValueShort;
	@Column(name = "put_trust_open_net", nullable = true)
	private Double putTrustOpenNet;
	@Column(name = "put_trust_open_value_net", nullable = true)
	private Double putTrustOpenValueNet;
	// foreign put option
	@Column(name = "put_foreign_trading_long", nullable = true)
	private Double putForeignTradingLong;
	@Column(name = "put_foreign_trading_value_long", nullable = true)
	private Double putForeignTradingValueLong;
	@Column(name = "put_foreign_trading_short", nullable = true)
	private Double putForeignTradingShort;
	@Column(name = "put_foreign_trading_value_short", nullable = true)
	private Double putForeignTradingValueShort;
	@Column(name = "put_foreign_trading_net", nullable = true)
	private Double putForeignTradingNet;
	@Column(name = "put_foreign_trading_value_net", nullable = true)
	private Double putForeignTradingValueNet;
	// foreign put option OI
	@Column(name = "put_foreign_open_long", nullable = true)
	private Double putForeignOpenLong;
	@Column(name = "put_foreign_open_value_long", nullable = true)
	private Double putForeignOpenValueLong;
	@Column(name = "put_foreign_open_short", nullable = true)
	private Double putForeignOpenShort;
	@Column(name = "put_foreign_open_value_short", nullable = true)
	private Double putForeignOpenValueShort;
	@Column(name = "put_foreign_open_net", nullable = true)
	private Double putForeignOpenNet;
	@Column(name = "put_foreign_open_value_net", nullable = true)
	private Double putForeignOpenValueNet;
	// current week data may be empty(the trading days within the 3rd week)
	// call option : current week contract
	// call option ： top5
	@Column(name = "call_buy_oi_top5_week", nullable = true)
	private Double callBuyOiTop5Week;
	@Column(name = "call_sell_oi_top5_week", nullable = true)
	private Double callSellOiTop5Week;
	// call option ： top10
	@Column(name = "call_buy_oi_top10_week", nullable = true)
	private Double callBuyOiTop10Week;
	@Column(name = "call_sell_oi_top10_week", nullable = true)
	private Double callSellOiTop10Week;
	// call option ： total OI 
	@Column(name = "call_total_oi_week", nullable = true)
	private Double callTotalOiWeek;
	// call option : current month contract
	// call option ： top5
	@Column(name = "call_buy_oi_top5", nullable = true)
	private Double callBuyOiTop5;
	@Column(name = "call_sell_oi_top5", nullable = true)
	private Double callSellOiTop5;
	// call option ： top10
	@Column(name = "call_buy_oi_top10", nullable = true)
	private Double callBuyOiTop10;
	@Column(name = "call_sell_oi_top10", nullable = true)
	private Double callSellOiTop10;
	// call option ： total OI
	@Column(name = "call_total_oi", nullable = true)
	private Double callTotalOi;
	// call option : all contracts
	// call option ： top5
	@Column(name = "call_buy_oi_top5_all", nullable = true)
	private Double callBuyOiTop5All;
	@Column(name = "call_sell_oi_top5_all", nullable = true)
	private Double callSellOiTop5All;
	// call option ： top10
	@Column(name = "call_buy_oi_top10_all", nullable = true)
	private Double callBuyOiTop10All;
	@Column(name = "call_sell_oi_top10_all", nullable = true)
	private Double callSellOiTop10All;
	// call option ： total OI
	@Column(name = "call_total_oi_all", nullable = true)
	private Double callTotalOiAll;
	//
	// put option : current week contract
	// put option ： top5
	@Column(name = "put_buy_oi_top5_week", nullable = true)
	private Double putBuyOiTop5Week;
	@Column(name = "put_sell_oi_top5_week", nullable = true)
	private Double putSellOiTop5Week;
	// put option ： top10
	@Column(name = "put_buy_oi_top10_week", nullable = true)
	private Double putBuyOiTop10Week;
	@Column(name = "put_sell_oi_top10_week", nullable = true)
	private Double putSellOiTop10Week;
	// put option ： total OI
	@Column(name = "put_total_oi_week", nullable = true)
	private Double putTotalOiWeek;
	// put option : current month contract
	// put option ： top5
	@Column(name = "put_buy_oi_top5", nullable = true)
	private Double putBuyOiTop5;
	@Column(name = "put_sell_oi_top5", nullable = true)
	private Double putSellOiTop5;
	// put option ： top10
	@Column(name = "put_buy_oi_top10", nullable = true)
	private Double putBuyOiTop10;
	@Column(name = "put_sell_oi_top10", nullable = true)
	private Double putSellOiTop10;
	// put option ： total OI
	@Column(name = "put_total_oi", nullable = true)
	private Double putTotalOi;
	// put option : all contracts
	// put option ： top5
	@Column(name = "put_buy_oi_top5_all", nullable = true)
	private Double putBuyOiTop5All;
	@Column(name = "put_sell_oi_top5_all", nullable = true)
	private Double putSellOiTop5All;
	// put option ： top10
	@Column(name = "put_buy_oi_top10_all", nullable = true)
	private Double putBuyOiTop10All;
	@Column(name = "put_sell_oi_top10_all", nullable = true)
	private Double putSellOiTop10All;
	// put option ： total OI
	@Column(name = "put_total_oi_all", nullable = true)
	private Double putTotalOiAll;

	public OptionData(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public OptionData() {
		super();
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public Double getCallTradingVolume() {
		return callTradingVolume;
	}

	public void setCallTradingVolume(Double callTradingVolume) {
		this.callTradingVolume = callTradingVolume;
	}

	public Double getPutTradingVolume() {
		return putTradingVolume;
	}

	public void setPutTradingVolume(Double putTradingVolume) {
		this.putTradingVolume = putTradingVolume;
	}

	public Double getCallOi() {
		return callOi;
	}

	public void setCallOi(Double callOi) {
		this.callOi = callOi;
	}

	public Double getPutOi() {
		return putOi;
	}

	public void setPutOi(Double putOi) {
		this.putOi = putOi;
	}

	public Double getCallDealerTradingLong() {
		return callDealerTradingLong;
	}

	public void setCallDealerTradingLong(Double callDealerTradingLong) {
		this.callDealerTradingLong = callDealerTradingLong;
	}

	public Double getCallDealerTradingValueLong() {
		return callDealerTradingValueLong;
	}

	public void setCallDealerTradingValueLong(Double callDealerTradingValueLong) {
		this.callDealerTradingValueLong = callDealerTradingValueLong;
	}

	public Double getCallDealerTradingShort() {
		return callDealerTradingShort;
	}

	public void setCallDealerTradingShort(Double callDealerTradingShort) {
		this.callDealerTradingShort = callDealerTradingShort;
	}

	public Double getCallDealerTradingValueShort() {
		return callDealerTradingValueShort;
	}

	public void setCallDealerTradingValueShort(Double callDealerTradingValueShort) {
		this.callDealerTradingValueShort = callDealerTradingValueShort;
	}

	public Double getCallDealerTradingNet() {
		return callDealerTradingNet;
	}

	public void setCallDealerTradingNet(Double callDealerTradingNet) {
		this.callDealerTradingNet = callDealerTradingNet;
	}

	public Double getCallDealerTradingValueNet() {
		return callDealerTradingValueNet;
	}

	public void setCallDealerTradingValueNet(Double callDealerTradingValueNet) {
		this.callDealerTradingValueNet = callDealerTradingValueNet;
	}

	public Double getCallDealerOpenLong() {
		return callDealerOpenLong;
	}

	public void setCallDealerOpenLong(Double callDealerOpenLong) {
		this.callDealerOpenLong = callDealerOpenLong;
	}

	public Double getCallDealerOpenValueLong() {
		return callDealerOpenValueLong;
	}

	public void setCallDealerOpenValueLong(Double callDealerOpenValueLong) {
		this.callDealerOpenValueLong = callDealerOpenValueLong;
	}

	public Double getCallDealerOpenShort() {
		return callDealerOpenShort;
	}

	public void setCallDealerOpenShort(Double callDealerOpenShort) {
		this.callDealerOpenShort = callDealerOpenShort;
	}

	public Double getCallDealerOpenValueShort() {
		return callDealerOpenValueShort;
	}

	public void setCallDealerOpenValueShort(Double callDealerOpenValueShort) {
		this.callDealerOpenValueShort = callDealerOpenValueShort;
	}

	public Double getCallDealerOpenNet() {
		return callDealerOpenNet;
	}

	public void setCallDealerOpenNet(Double callDealerOpenNet) {
		this.callDealerOpenNet = callDealerOpenNet;
	}

	public Double getCallDealerOpenValueNet() {
		return callDealerOpenValueNet;
	}

	public void setCallDealerOpenValueNet(Double callDealerOpenValueNet) {
		this.callDealerOpenValueNet = callDealerOpenValueNet;
	}

	public Double getCallTrustTradingLong() {
		return callTrustTradingLong;
	}

	public void setCallTrustTradingLong(Double callTrustTradingLong) {
		this.callTrustTradingLong = callTrustTradingLong;
	}

	public Double getCallTrustTradingValueLong() {
		return callTrustTradingValueLong;
	}

	public void setCallTrustTradingValueLong(Double callTrustTradingValueLong) {
		this.callTrustTradingValueLong = callTrustTradingValueLong;
	}

	public Double getCallTrustTradingShort() {
		return callTrustTradingShort;
	}

	public void setCallTrustTradingShort(Double callTrustTradingShort) {
		this.callTrustTradingShort = callTrustTradingShort;
	}

	public Double getCallTrustTradingValueShort() {
		return callTrustTradingValueShort;
	}

	public void setCallTrustTradingValueShort(Double callTrustTradingValueShort) {
		this.callTrustTradingValueShort = callTrustTradingValueShort;
	}

	public Double getCallTrustTradingNet() {
		return callTrustTradingNet;
	}

	public void setCallTrustTradingNet(Double callTrustTradingNet) {
		this.callTrustTradingNet = callTrustTradingNet;
	}

	public Double getCallTrustTradingValueNet() {
		return callTrustTradingValueNet;
	}

	public void setCallTrustTradingValueNet(Double callTrustTradingValueNet) {
		this.callTrustTradingValueNet = callTrustTradingValueNet;
	}

	public Double getCallTrustOpenLong() {
		return callTrustOpenLong;
	}

	public void setCallTrustOpenLong(Double callTrustOpenLong) {
		this.callTrustOpenLong = callTrustOpenLong;
	}

	public Double getCallTrustOpenValueLong() {
		return callTrustOpenValueLong;
	}

	public void setCallTrustOpenValueLong(Double callTrustOpenValueLong) {
		this.callTrustOpenValueLong = callTrustOpenValueLong;
	}

	public Double getCallTrustOpenShort() {
		return callTrustOpenShort;
	}

	public void setCallTrustOpenShort(Double callTrustOpenShort) {
		this.callTrustOpenShort = callTrustOpenShort;
	}

	public Double getCallTrustOpenValueShort() {
		return callTrustOpenValueShort;
	}

	public void setCallTrustOpenValueShort(Double callTrustOpenValueShort) {
		this.callTrustOpenValueShort = callTrustOpenValueShort;
	}

	public Double getCallTrustOpenNet() {
		return callTrustOpenNet;
	}

	public void setCallTrustOpenNet(Double callTrustOpenNet) {
		this.callTrustOpenNet = callTrustOpenNet;
	}

	public Double getCallTrustOpenValueNet() {
		return callTrustOpenValueNet;
	}

	public void setCallTrustOpenValueNet(Double callTrustOpenValueNet) {
		this.callTrustOpenValueNet = callTrustOpenValueNet;
	}

	public Double getCallForeignTradingLong() {
		return callForeignTradingLong;
	}

	public void setCallForeignTradingLong(Double callForeignTradingLong) {
		this.callForeignTradingLong = callForeignTradingLong;
	}

	public Double getCallForeignTradingValueLong() {
		return callForeignTradingValueLong;
	}

	public void setCallForeignTradingValueLong(Double callForeignTradingValueLong) {
		this.callForeignTradingValueLong = callForeignTradingValueLong;
	}

	public Double getCallForeignTradingShort() {
		return callForeignTradingShort;
	}

	public void setCallForeignTradingShort(Double callForeignTradingShort) {
		this.callForeignTradingShort = callForeignTradingShort;
	}

	public Double getCallForeignTradingValueShort() {
		return callForeignTradingValueShort;
	}

	public void setCallForeignTradingValueShort(Double callForeignTradingValueShort) {
		this.callForeignTradingValueShort = callForeignTradingValueShort;
	}

	public Double getCallForeignTradingNet() {
		return callForeignTradingNet;
	}

	public void setCallForeignTradingNet(Double callForeignTradingNet) {
		this.callForeignTradingNet = callForeignTradingNet;
	}

	public Double getCallForeignTradingValueNet() {
		return callForeignTradingValueNet;
	}

	public void setCallForeignTradingValueNet(Double callForeignTradingValueNet) {
		this.callForeignTradingValueNet = callForeignTradingValueNet;
	}

	public Double getCallForeignOpenLong() {
		return callForeignOpenLong;
	}

	public void setCallForeignOpenLong(Double callForeignOpenLong) {
		this.callForeignOpenLong = callForeignOpenLong;
	}

	public Double getCallForeignOpenValueLong() {
		return callForeignOpenValueLong;
	}

	public void setCallForeignOpenValueLong(Double callForeignOpenValueLong) {
		this.callForeignOpenValueLong = callForeignOpenValueLong;
	}

	public Double getCallForeignOpenShort() {
		return callForeignOpenShort;
	}

	public void setCallForeignOpenShort(Double callForeignOpenShort) {
		this.callForeignOpenShort = callForeignOpenShort;
	}

	public Double getCallForeignOpenValueShort() {
		return callForeignOpenValueShort;
	}

	public void setCallForeignOpenValueShort(Double callForeignOpenValueShort) {
		this.callForeignOpenValueShort = callForeignOpenValueShort;
	}

	public Double getCallForeignOpenNet() {
		return callForeignOpenNet;
	}

	public void setCallForeignOpenNet(Double callForeignOpenNet) {
		this.callForeignOpenNet = callForeignOpenNet;
	}

	public Double getCallForeignOpenValueNet() {
		return callForeignOpenValueNet;
	}

	public void setCallForeignOpenValueNet(Double callForeignOpenValueNet) {
		this.callForeignOpenValueNet = callForeignOpenValueNet;
	}

	public Double getPutDealerTradingLong() {
		return putDealerTradingLong;
	}

	public void setPutDealerTradingLong(Double putDealerTradingLong) {
		this.putDealerTradingLong = putDealerTradingLong;
	}

	public Double getPutDealerTradingValueLong() {
		return putDealerTradingValueLong;
	}

	public void setPutDealerTradingValueLong(Double putDealerTradingValueLong) {
		this.putDealerTradingValueLong = putDealerTradingValueLong;
	}

	public Double getPutDealerTradingShort() {
		return putDealerTradingShort;
	}

	public void setPutDealerTradingShort(Double putDealerTradingShort) {
		this.putDealerTradingShort = putDealerTradingShort;
	}

	public Double getPutDealerTradingValueShort() {
		return putDealerTradingValueShort;
	}

	public void setPutDealerTradingValueShort(Double putDealerTradingValueShort) {
		this.putDealerTradingValueShort = putDealerTradingValueShort;
	}

	public Double getPutDealerTradingNet() {
		return putDealerTradingNet;
	}

	public void setPutDealerTradingNet(Double putDealerTradingNet) {
		this.putDealerTradingNet = putDealerTradingNet;
	}

	public Double getPutDealerTradingValueNet() {
		return putDealerTradingValueNet;
	}

	public void setPutDealerTradingValueNet(Double putDealerTradingValueNet) {
		this.putDealerTradingValueNet = putDealerTradingValueNet;
	}

	public Double getPutDealerOpenLong() {
		return putDealerOpenLong;
	}

	public void setPutDealerOpenLong(Double putDealerOpenLong) {
		this.putDealerOpenLong = putDealerOpenLong;
	}

	public Double getPutDealerOpenValueLong() {
		return putDealerOpenValueLong;
	}

	public void setPutDealerOpenValueLong(Double putDealerOpenValueLong) {
		this.putDealerOpenValueLong = putDealerOpenValueLong;
	}

	public Double getPutDealerOpenShort() {
		return putDealerOpenShort;
	}

	public void setPutDealerOpenShort(Double putDealerOpenShort) {
		this.putDealerOpenShort = putDealerOpenShort;
	}

	public Double getPutDealerOpenValueShort() {
		return putDealerOpenValueShort;
	}

	public void setPutDealerOpenValueShort(Double putDealerOpenValueShort) {
		this.putDealerOpenValueShort = putDealerOpenValueShort;
	}

	public Double getPutDealerOpenNet() {
		return putDealerOpenNet;
	}

	public void setPutDealerOpenNet(Double putDealerOpenNet) {
		this.putDealerOpenNet = putDealerOpenNet;
	}

	public Double getPutDealerOpenValueNet() {
		return putDealerOpenValueNet;
	}

	public void setPutDealerOpenValueNet(Double putDealerOpenValueNet) {
		this.putDealerOpenValueNet = putDealerOpenValueNet;
	}

	public Double getPutTrustTradingLong() {
		return putTrustTradingLong;
	}

	public void setPutTrustTradingLong(Double putTrustTradingLong) {
		this.putTrustTradingLong = putTrustTradingLong;
	}

	public Double getPutTrustTradingValueLong() {
		return putTrustTradingValueLong;
	}

	public void setPutTrustTradingValueLong(Double putTrustTradingValueLong) {
		this.putTrustTradingValueLong = putTrustTradingValueLong;
	}

	public Double getPutTrustTradingShort() {
		return putTrustTradingShort;
	}

	public void setPutTrustTradingShort(Double putTrustTradingShort) {
		this.putTrustTradingShort = putTrustTradingShort;
	}

	public Double getPutTrustTradingValueShort() {
		return putTrustTradingValueShort;
	}

	public void setPutTrustTradingValueShort(Double putTrustTradingValueShort) {
		this.putTrustTradingValueShort = putTrustTradingValueShort;
	}

	public Double getPutTrustTradingNet() {
		return putTrustTradingNet;
	}

	public void setPutTrustTradingNet(Double putTrustTradingNet) {
		this.putTrustTradingNet = putTrustTradingNet;
	}

	public Double getPutTrustTradingValueNet() {
		return putTrustTradingValueNet;
	}

	public void setPutTrustTradingValueNet(Double putTrustTradingValueNet) {
		this.putTrustTradingValueNet = putTrustTradingValueNet;
	}

	public Double getPutTrustOpenLong() {
		return putTrustOpenLong;
	}

	public void setPutTrustOpenLong(Double putTrustOpenLong) {
		this.putTrustOpenLong = putTrustOpenLong;
	}

	public Double getPutTrustOpenValueLong() {
		return putTrustOpenValueLong;
	}

	public void setPutTrustOpenValueLong(Double putTrustOpenValueLong) {
		this.putTrustOpenValueLong = putTrustOpenValueLong;
	}

	public Double getPutTrustOpenShort() {
		return putTrustOpenShort;
	}

	public void setPutTrustOpenShort(Double putTrustOpenShort) {
		this.putTrustOpenShort = putTrustOpenShort;
	}

	public Double getPutTrustOpenValueShort() {
		return putTrustOpenValueShort;
	}

	public void setPutTrustOpenValueShort(Double putTrustOpenValueShort) {
		this.putTrustOpenValueShort = putTrustOpenValueShort;
	}

	public Double getPutTrustOpenNet() {
		return putTrustOpenNet;
	}

	public void setPutTrustOpenNet(Double putTrustOpenNet) {
		this.putTrustOpenNet = putTrustOpenNet;
	}

	public Double getPutTrustOpenValueNet() {
		return putTrustOpenValueNet;
	}

	public void setPutTrustOpenValueNet(Double putTrustOpenValueNet) {
		this.putTrustOpenValueNet = putTrustOpenValueNet;
	}

	public Double getPutForeignTradingLong() {
		return putForeignTradingLong;
	}

	public void setPutForeignTradingLong(Double putForeignTradingLong) {
		this.putForeignTradingLong = putForeignTradingLong;
	}

	public Double getPutForeignTradingValueLong() {
		return putForeignTradingValueLong;
	}

	public void setPutForeignTradingValueLong(Double putForeignTradingValueLong) {
		this.putForeignTradingValueLong = putForeignTradingValueLong;
	}

	public Double getPutForeignTradingShort() {
		return putForeignTradingShort;
	}

	public void setPutForeignTradingShort(Double putForeignTradingShort) {
		this.putForeignTradingShort = putForeignTradingShort;
	}

	public Double getPutForeignTradingValueShort() {
		return putForeignTradingValueShort;
	}

	public void setPutForeignTradingValueShort(Double putForeignTradingValueShort) {
		this.putForeignTradingValueShort = putForeignTradingValueShort;
	}

	public Double getPutForeignTradingNet() {
		return putForeignTradingNet;
	}

	public void setPutForeignTradingNet(Double putForeignTradingNet) {
		this.putForeignTradingNet = putForeignTradingNet;
	}

	public Double getPutForeignTradingValueNet() {
		return putForeignTradingValueNet;
	}

	public void setPutForeignTradingValueNet(Double putForeignTradingValueNet) {
		this.putForeignTradingValueNet = putForeignTradingValueNet;
	}

	public Double getPutForeignOpenLong() {
		return putForeignOpenLong;
	}

	public void setPutForeignOpenLong(Double putForeignOpenLong) {
		this.putForeignOpenLong = putForeignOpenLong;
	}

	public Double getPutForeignOpenValueLong() {
		return putForeignOpenValueLong;
	}

	public void setPutForeignOpenValueLong(Double putForeignOpenValueLong) {
		this.putForeignOpenValueLong = putForeignOpenValueLong;
	}

	public Double getPutForeignOpenShort() {
		return putForeignOpenShort;
	}

	public void setPutForeignOpenShort(Double putForeignOpenShort) {
		this.putForeignOpenShort = putForeignOpenShort;
	}

	public Double getPutForeignOpenValueShort() {
		return putForeignOpenValueShort;
	}

	public void setPutForeignOpenValueShort(Double putForeignOpenValueShort) {
		this.putForeignOpenValueShort = putForeignOpenValueShort;
	}

	public Double getPutForeignOpenNet() {
		return putForeignOpenNet;
	}

	public void setPutForeignOpenNet(Double putForeignOpenNet) {
		this.putForeignOpenNet = putForeignOpenNet;
	}

	public Double getPutForeignOpenValueNet() {
		return putForeignOpenValueNet;
	}

	public void setPutForeignOpenValueNet(Double putForeignOpenValueNet) {
		this.putForeignOpenValueNet = putForeignOpenValueNet;
	}

	public Double getCallBuyOiTop5Week() {
		return callBuyOiTop5Week;
	}

	public void setCallBuyOiTop5Week(Double callBuyOiTop5Week) {
		this.callBuyOiTop5Week = callBuyOiTop5Week;
	}

	public Double getCallSellOiTop5Week() {
		return callSellOiTop5Week;
	}

	public void setCallSellOiTop5Week(Double callSellOiTop5Week) {
		this.callSellOiTop5Week = callSellOiTop5Week;
	}

	public Double getCallBuyOiTop10Week() {
		return callBuyOiTop10Week;
	}

	public void setCallBuyOiTop10Week(Double callBuyOiTop10Week) {
		this.callBuyOiTop10Week = callBuyOiTop10Week;
	}

	public Double getCallSellOiTop10Week() {
		return callSellOiTop10Week;
	}

	public void setCallSellOiTop10Week(Double callSellOiTop10Week) {
		this.callSellOiTop10Week = callSellOiTop10Week;
	}

	public Double getCallTotalOiWeek() {
		return callTotalOiWeek;
	}

	public void setCallTotalOiWeek(Double callTotalOiWeek) {
		this.callTotalOiWeek = callTotalOiWeek;
	}

	public Double getCallBuyOiTop5() {
		return callBuyOiTop5;
	}

	public void setCallBuyOiTop5(Double callBuyOiTop5) {
		this.callBuyOiTop5 = callBuyOiTop5;
	}

	public Double getCallSellOiTop5() {
		return callSellOiTop5;
	}

	public void setCallSellOiTop5(Double callSellOiTop5) {
		this.callSellOiTop5 = callSellOiTop5;
	}

	public Double getCallBuyOiTop10() {
		return callBuyOiTop10;
	}

	public void setCallBuyOiTop10(Double callBuyOiTop10) {
		this.callBuyOiTop10 = callBuyOiTop10;
	}

	public Double getCallSellOiTop10() {
		return callSellOiTop10;
	}

	public void setCallSellOiTop10(Double callSellOiTop10) {
		this.callSellOiTop10 = callSellOiTop10;
	}

	public Double getCallTotalOi() {
		return callTotalOi;
	}

	public void setCallTotalOi(Double callTotalOi) {
		this.callTotalOi = callTotalOi;
	}

	public Double getCallBuyOiTop5All() {
		return callBuyOiTop5All;
	}

	public void setCallBuyOiTop5All(Double callBuyOiTop5All) {
		this.callBuyOiTop5All = callBuyOiTop5All;
	}

	public Double getCallSellOiTop5All() {
		return callSellOiTop5All;
	}

	public void setCallSellOiTop5All(Double callSellOiTop5All) {
		this.callSellOiTop5All = callSellOiTop5All;
	}

	public Double getCallBuyOiTop10All() {
		return callBuyOiTop10All;
	}

	public void setCallBuyOiTop10All(Double callBuyOiTop10All) {
		this.callBuyOiTop10All = callBuyOiTop10All;
	}

	public Double getCallSellOiTop10All() {
		return callSellOiTop10All;
	}

	public void setCallSellOiTop10All(Double callSellOiTop10All) {
		this.callSellOiTop10All = callSellOiTop10All;
	}

	public Double getCallTotalOiAll() {
		return callTotalOiAll;
	}

	public void setCallTotalOiAll(Double callTotalOiAll) {
		this.callTotalOiAll = callTotalOiAll;
	}

	public Double getPutBuyOiTop5Week() {
		return putBuyOiTop5Week;
	}

	public void setPutBuyOiTop5Week(Double putBuyOiTop5Week) {
		this.putBuyOiTop5Week = putBuyOiTop5Week;
	}

	public Double getPutSellOiTop5Week() {
		return putSellOiTop5Week;
	}

	public void setPutSellOiTop5Week(Double putSellOiTop5Week) {
		this.putSellOiTop5Week = putSellOiTop5Week;
	}

	public Double getPutBuyOiTop10Week() {
		return putBuyOiTop10Week;
	}

	public void setPutBuyOiTop10Week(Double putBuyOiTop10Week) {
		this.putBuyOiTop10Week = putBuyOiTop10Week;
	}

	public Double getPutSellOiTop10Week() {
		return putSellOiTop10Week;
	}

	public void setPutSellOiTop10Week(Double putSellOiTop10Week) {
		this.putSellOiTop10Week = putSellOiTop10Week;
	}

	public Double getPutTotalOiWeek() {
		return putTotalOiWeek;
	}

	public void setPutTotalOiWeek(Double putTotalOiWeek) {
		this.putTotalOiWeek = putTotalOiWeek;
	}

	public Double getPutBuyOiTop5() {
		return putBuyOiTop5;
	}

	public void setPutBuyOiTop5(Double putBuyOiTop5) {
		this.putBuyOiTop5 = putBuyOiTop5;
	}

	public Double getPutSellOiTop5() {
		return putSellOiTop5;
	}

	public void setPutSellOiTop5(Double putSellOiTop5) {
		this.putSellOiTop5 = putSellOiTop5;
	}

	public Double getPutBuyOiTop10() {
		return putBuyOiTop10;
	}

	public void setPutBuyOiTop10(Double putBuyOiTop10) {
		this.putBuyOiTop10 = putBuyOiTop10;
	}

	public Double getPutSellOiTop10() {
		return putSellOiTop10;
	}

	public void setPutSellOiTop10(Double putSellOiTop10) {
		this.putSellOiTop10 = putSellOiTop10;
	}

	public Double getPutTotalOi() {
		return putTotalOi;
	}

	public void setPutTotalOi(Double putTotalOi) {
		this.putTotalOi = putTotalOi;
	}

	public Double getPutBuyOiTop5All() {
		return putBuyOiTop5All;
	}

	public void setPutBuyOiTop5All(Double putBuyOiTop5All) {
		this.putBuyOiTop5All = putBuyOiTop5All;
	}

	public Double getPutSellOiTop5All() {
		return putSellOiTop5All;
	}

	public void setPutSellOiTop5All(Double putSellOiTop5All) {
		this.putSellOiTop5All = putSellOiTop5All;
	}

	public Double getPutBuyOiTop10All() {
		return putBuyOiTop10All;
	}

	public void setPutBuyOiTop10All(Double putBuyOiTop10All) {
		this.putBuyOiTop10All = putBuyOiTop10All;
	}

	public Double getPutSellOiTop10All() {
		return putSellOiTop10All;
	}

	public void setPutSellOiTop10All(Double putSellOiTop10All) {
		this.putSellOiTop10All = putSellOiTop10All;
	}

	public Double getPutTotalOiAll() {
		return putTotalOiAll;
	}

	public void setPutTotalOiAll(Double putTotalOiAll) {
		this.putTotalOiAll = putTotalOiAll;
	}

	public Long getId() {
		return id;
	}
}
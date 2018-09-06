package com.javatican.stock.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "option_series_data")
public class OptionSeriesData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private Date tradingDate;

	@Column(name = "strike_price", nullable = false)
	private Integer strikePrice;

	@Column(name = "call_option", nullable = false)
	private Boolean callOption;

	@Column(name = "week_option", nullable = false)
	private Boolean weekOption = false;

	@Column(name = "current_month_option", nullable = false)
	private Boolean currentMonthOption = false;

	@Column(name = "next_month_option", nullable = false)
	private Boolean nextMonthOption = false;

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

	public OptionSeriesData(Date tradingDate, Integer strikePrice, Boolean callOption, Boolean weekOption,
			Boolean currentMonthOption, Boolean nextMonthOption) {
		super();
		this.tradingDate = tradingDate;
		this.strikePrice = strikePrice;
		this.callOption = callOption;
		this.weekOption = weekOption;
		this.currentMonthOption = currentMonthOption;
		this.nextMonthOption = nextMonthOption;
	}

	public OptionSeriesData(Date tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public OptionSeriesData() {
		super();
	}

	public Date getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Date tradingDate) {
		this.tradingDate = tradingDate;
	}

	public Integer getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(Integer strikePrice) {
		this.strikePrice = strikePrice;
	}

	public Boolean getCallOption() {
		return callOption;
	}

	public void setCallOption(Boolean callOption) {
		this.callOption = callOption;
	}

	public Boolean getWeekOption() {
		return weekOption;
	}

	public void setWeekOption(Boolean weekOption) {
		this.weekOption = weekOption;
	}

	public Boolean getCurrentMonthOption() {
		return currentMonthOption;
	}

	public void setCurrentMonthOption(Boolean currentMonthOption) {
		this.currentMonthOption = currentMonthOption;
	}

	public Boolean getNextMonthOption() {
		return nextMonthOption;
	}

	public void setNextMonthOption(Boolean nextMonthOption) {
		this.nextMonthOption = nextMonthOption;
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

	public Long getId() {
		return id;
	}

}
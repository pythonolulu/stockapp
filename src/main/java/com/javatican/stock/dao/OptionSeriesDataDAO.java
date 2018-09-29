package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.OptionSeriesData;
import com.javatican.stock.repository.OptionSeriesDataRepository;

@Repository("optionSeriesDataDAO")
public class OptionSeriesDataDAO {
	@Autowired
	OptionSeriesDataRepository optionSeriesDataRepository;

	public OptionSeriesDataDAO() {
	}

	public boolean existsByTradingDate(Date tradingDate) {
		return optionSeriesDataRepository.existsByTradingDate(tradingDate);
	}

	public OptionSeriesData getByTradingDate(Date tradingDate) {
		return optionSeriesDataRepository.getByTradingDate(tradingDate);
	}

	public List<OptionSeriesData> findAll() {
		return optionSeriesDataRepository.findAllByOrderByTradingDate();
	}

	public void save(OptionSeriesData optionSeriesData) {
		optionSeriesDataRepository.save(optionSeriesData);
	}

	public void saveAll(List<OptionSeriesData> fdList) {
		optionSeriesDataRepository.saveAll(fdList);
	}

	public Date getLatestTradingDate() {
		return optionSeriesDataRepository.getLatestTradingDate();
	}

	public List<Integer> findCallOptionStrikePriceBetween(Integer low, Integer high) {
		return optionSeriesDataRepository.findCallOptionStrikePriceBetween(low, high);
	}

	public List<Integer> findPutOptionStrikePriceBetween(Integer low, Integer high) {
		return optionSeriesDataRepository.findPutOptionStrikePriceBetween(low, high);
	}

	public List<OptionSeriesData> findWeekCallOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(true, strikePrice, true, false, false);
	}

	public List<OptionSeriesData> findWeekPutOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(false, strikePrice, true, false, false);
	}

	public List<OptionSeriesData> findCurrentMonthCallOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(true, strikePrice, false, true, false);
	}

	public List<OptionSeriesData> findCurrentMonthPutOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(false, strikePrice, false, true, false);
	}

	public List<OptionSeriesData> findNextMonthCallOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(true, strikePrice, false, false, true);
	}

	public List<OptionSeriesData> findNextMonthPutOptionByStrikePrice(Integer strikePrice) {
		return optionSeriesDataRepository.customQuery(false, strikePrice, false, false, true);
	}
	//

	public List<OptionSeriesData> findWeekCallOptionByStrikePriceDateSince(Integer strikePrice, Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(true, strikePrice, true, false, false, dateSince);
	}

	public List<OptionSeriesData> findWeekPutOptionByStrikePriceDateSince(Integer strikePrice, Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(false, strikePrice, true, false, false, dateSince);
	}

	public List<OptionSeriesData> findCurrentMonthCallOptionByStrikePriceDateSince(Integer strikePrice,
			Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(true, strikePrice, false, true, false, dateSince);
	}

	public List<OptionSeriesData> findCurrentMonthPutOptionByStrikePriceDateSince(Integer strikePrice, Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(false, strikePrice, false, true, false, dateSince);
	}

	public List<OptionSeriesData> findNextMonthCallOptionByStrikePriceDateSince(Integer strikePrice, Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(true, strikePrice, false, false, true, dateSince);
	}

	public List<OptionSeriesData> findNextMonthPutOptionByStrikePriceDateSince(Integer strikePrice, Date dateSince) {
		return optionSeriesDataRepository.customQueryDateSince(false, strikePrice, false, false, true, dateSince);
	}
}

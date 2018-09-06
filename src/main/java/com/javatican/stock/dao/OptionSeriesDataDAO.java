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
}

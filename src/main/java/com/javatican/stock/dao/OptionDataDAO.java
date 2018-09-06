package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.OptionData;
import com.javatican.stock.repository.OptionDataRepository;

@Repository("optionDataDAO")
public class OptionDataDAO {
	@Autowired
	OptionDataRepository optionDataRepository;

	public OptionDataDAO() {
	}

	public boolean existsByTradingDate(Date tradingDate) {
		return optionDataRepository.existsByTradingDate(tradingDate);
	}

	public OptionData getByTradingDate(Date tradingDate) {
		return optionDataRepository.getByTradingDate(tradingDate);
	}

	public List<OptionData> findAll() {
		return optionDataRepository.findAllByOrderByTradingDate();
	}

	public void save(OptionData optionData) {
		optionDataRepository.save(optionData);
	}

	public void saveAll(List<OptionData> fdList) {
		optionDataRepository.saveAll(fdList);
	}

	public Date getLatestTradingDate() {
		return optionDataRepository.getLatestTradingDate();
	}
}

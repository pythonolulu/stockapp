package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.WeeklyTradingValue;
import com.javatican.stock.repository.WeeklyTradingValueRepository;

@Repository("weeklyTradingValueDAO")
public class WeeklyTradingValueDAO {
	@Autowired
	WeeklyTradingValueRepository weeklyTradingValueRepository;

	public WeeklyTradingValueDAO() {
	}

	public boolean existsByTradingDate(Date tradingDate) {
		return weeklyTradingValueRepository.existsByTradingDate(tradingDate);
	}

	public WeeklyTradingValue getByTradingDate(Date tradingDate) {
		return weeklyTradingValueRepository.getByTradingDate(tradingDate);
	}

	public List<WeeklyTradingValue> findAll() {
		return weeklyTradingValueRepository.findAllByOrderByTradingDate();
	}

	public void save(WeeklyTradingValue wtv) {
		weeklyTradingValueRepository.save(wtv);
	}

	public void saveAll(List<WeeklyTradingValue> wtvList) {
		weeklyTradingValueRepository.saveAll(wtvList);
	}

}

package com.javatican.stock.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.WeeklyTradingValue;
import com.javatican.stock.repository.WeeklyTradingValueRepository;
import com.javatican.stock.util.StockUtils;

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

	public Map<String, WeeklyTradingValue> findAllAsMap() {
		Map<String, WeeklyTradingValue> wtvMap = new TreeMap<>();
		weeklyTradingValueRepository.findAllByOrderByTradingDate()
				.forEach(item -> wtvMap.put(StockUtils.dateToSimpleString(item.getTradingDate()), item));
		return wtvMap;
	}

	public void save(WeeklyTradingValue wtv) {
		weeklyTradingValueRepository.save(wtv);
	}

	public void saveAll(Collection<WeeklyTradingValue> wtvCollection) {
		weeklyTradingValueRepository.saveAll(wtvCollection);
	}

	public void deleteAll(List<WeeklyTradingValue> wtvList) {
		weeklyTradingValueRepository.deleteAll(wtvList);
	}

}

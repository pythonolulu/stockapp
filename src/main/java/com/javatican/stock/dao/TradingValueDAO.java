package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingValue;
import com.javatican.stock.repository.TradingValueRepository;

@Repository("tradingValueDAO")
public class TradingValueDAO {
	@Autowired
	TradingValueRepository tradingValueRepository;
	
	public TradingValueDAO() {
	}
	
	public boolean existsByTradingDate(Date tradingDate) {
		return tradingValueRepository.existsByTradingDate(tradingDate);
	}

	public TradingValue getByTradingDate(Date tradingDate) {
		return tradingValueRepository.getByTradingDate(tradingDate);
	}

	public List<TradingValue> findAll() {
		return tradingValueRepository.findAllByOrderByTradingDate();
	}
	public void save(TradingValue tradingValue) {
		tradingValueRepository.save(tradingValue);
	}
	public void saveAll(List<TradingValue> tvList) {
		tradingValueRepository.saveAll(tvList);
	}
	
	

}

package com.javatican.stock;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingValue;

@Repository("tradingValueDAO")
public class TradingValueDAO {
	@Autowired
	TradingValueRepository tradingValueRepository;
	
	public TradingValueDAO() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean existsByTradingDate(Date tradingDate) {
		return tradingValueRepository.existsByTradingDate(tradingDate);
	}
	public void save(TradingValue tradingValue) {
		tradingValueRepository.save(tradingValue);
	}
	

}

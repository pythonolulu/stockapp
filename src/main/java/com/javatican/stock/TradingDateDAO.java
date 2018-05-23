package com.javatican.stock;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingDate;

@Repository("tradingDateDAO")
public class TradingDateDAO {
	public TradingDateDAO() {
		// TODO Auto-generated constructor stub
	}
	@Autowired
	TradingDateRepository tradingDateRepository;
		
	public void saveTradingDate(TradingDate tradingDate){
		tradingDateRepository.save(tradingDate);
	}
	public boolean existsByDate(Date date) {
		return tradingDateRepository.existsByDate(date);
	}
	
}

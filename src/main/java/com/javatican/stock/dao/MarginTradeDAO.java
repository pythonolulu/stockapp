package com.javatican.stock.dao;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.MarginTrade;
import com.javatican.stock.repository.MarginTradeRepository;

@Repository("marginTradeDAO")
public class MarginTradeDAO {
	@Autowired
	MarginTradeRepository marginTradeRepository;
	
	public MarginTradeDAO() {
	}
	
	public boolean existsByTradingDate(Date tradingDate) {
		return marginTradeRepository.existsByTradingDate(tradingDate);
	}
	public void save(MarginTrade marginTrade) {
		marginTradeRepository.save(marginTrade);
	}
	

}

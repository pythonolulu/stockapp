package com.javatican.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingDate;

@Repository("stockDAO")
public class StockDAO {
	public StockDAO() {
		// TODO Auto-generated constructor stub
	}
	@Autowired
	TradingDateRepository tradingDateRepository;
	
	public void saveTradingDate(TradingDate date){
		tradingDateRepository.save(date);
	}

}

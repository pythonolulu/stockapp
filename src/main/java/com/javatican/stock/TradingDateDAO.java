package com.javatican.stock;

import java.util.Date;
import java.util.List;

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
		
	public void save(TradingDate tradingDate){
		tradingDateRepository.save(tradingDate);
	}
	public boolean existsByDate(Date date) {
		return tradingDateRepository.existsByDate(date);
	}
	public List<TradingDate> findAll(){
		return tradingDateRepository.findAll();
	}
	public List<TradingDate> findBetween(Date begin, Date end){
		return tradingDateRepository.findByDateBetween(begin, end);
	}

	public List<TradingDate> findAfter(Date date){
		return tradingDateRepository.findByDateAfter(date);
	}
	
}

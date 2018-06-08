package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingDate;
import com.javatican.stock.repository.TradingDateRepository;

@Repository("tradingDateDAO")
public class TradingDateDAO {
	public TradingDateDAO() {
	}

	@Autowired
	TradingDateRepository tradingDateRepository;

	public void save(TradingDate tradingDate) {
		tradingDateRepository.save(tradingDate);
	}

	public boolean existsByDate(Date date) {
		return tradingDateRepository.existsByDate(date);
	}

	public List<TradingDate> findAll() {
		return tradingDateRepository.findAll();
	}

	public List<TradingDate> findBetween(Date begin, Date end) {
		return tradingDateRepository.findByDateBetween(begin, end);
	}

	public List<TradingDate> findAfter(Date date) {
		return tradingDateRepository.findByDateAfter(date);
	}
	public Date getLatestTradingDate() {
		return tradingDateRepository.findTopByOrderByDateDesc().getDate();
	}
	
	public List<Date> findLatestNTradingDate(int length){
		Pageable pageable = PageRequest.of(0, length);
		return tradingDateRepository.findLatestNTradingDate(pageable);
		
	}
 
}

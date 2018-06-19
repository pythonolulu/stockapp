package com.javatican.stock.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.repository.CallWarrantTradeSummaryRepository;

@Repository("callWarrantTradeSummaryDAO")
public class CallWarrantTradeSummaryDAO {
	@Autowired
	CallWarrantTradeSummaryRepository callWarrantTradeSummaryRepository;

	public CallWarrantTradeSummaryDAO() {
	}

	public void save(CallWarrantTradeSummary callWarrantTradeSummary) {
		callWarrantTradeSummaryRepository.save(callWarrantTradeSummary);
	}

	public void saveAll(Collection<CallWarrantTradeSummary> list) {
		callWarrantTradeSummaryRepository.saveAll(list);
	}

	public List<CallWarrantTradeSummary> getByTradingDate(Date tradingDate) {
		return callWarrantTradeSummaryRepository.findByTradingDate(tradingDate);
	}

	public Date getLatestTradingDate() {
		return callWarrantTradeSummaryRepository.getLatestTradingDate();
	}

	public List<String> getStockSymbolsWithCallWarrant() {
		return callWarrantTradeSummaryRepository.getStockSymbolsWithCallWarrant();
	}

}

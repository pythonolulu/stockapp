package com.javatican.stock.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.PutWarrantTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.repository.PutWarrantTradeSummaryRepository;

@Repository("putWarrantTradeSummaryDAO")
public class PutWarrantTradeSummaryDAO {
	@Autowired
	PutWarrantTradeSummaryRepository putWarrantTradeSummaryRepository;

	public PutWarrantTradeSummaryDAO() {
	}

	public void save(PutWarrantTradeSummary putWarrantTradeSummary) {
		putWarrantTradeSummaryRepository.save(putWarrantTradeSummary);
	}

	public void saveAll(Collection<PutWarrantTradeSummary> list) {
		putWarrantTradeSummaryRepository.saveAll(list);
	}

	public List<PutWarrantTradeSummary> getByTradingDate(Date tradingDate) {
		return putWarrantTradeSummaryRepository.findByTradingDate(tradingDate);
	}

	public List<PutWarrantTradeSummary> getByStockSymbol(String stockSymbol) {
		return putWarrantTradeSummaryRepository.findByStockSymbolOrderByTradingDateAsc(stockSymbol);
	}

	public Date getLatestTradingDate() {
		return putWarrantTradeSummaryRepository.getLatestTradingDate();
	}

	public List<String> getStockSymbolsWithPutWarrant() {
		return putWarrantTradeSummaryRepository.getStockSymbolsWithPutWarrant();
	}
	public Map<String, StockItem> getStockItemsWithPutWarrant() {
		List<StockItem> siList = putWarrantTradeSummaryRepository.getStockItemsWithPutWarrant();
		Map<String,StockItem> siMap = new TreeMap<>();
		siList.stream().forEach(si->siMap.put(si.getSymbol(),si));
		return siMap;
	}

}

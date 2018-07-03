package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.StockItemLog;
import com.javatican.stock.repository.StockItemLogRepository;

@Repository("stockItemLogDAO")
public class StockItemLogDAO {
	public StockItemLogDAO() {
	}

	@Autowired
	StockItemLogRepository stockItemLogRepository;

	public StockItemLog save(StockItemLog si) {
		return stockItemLogRepository.save(si);
	}

	public Iterable<StockItemLog> saveAll(List<StockItemLog> silList) {
		return stockItemLogRepository.saveAll(silList);
	}

	public StockItemLog findBySymbol(String symbol) {
		return stockItemLogRepository.findBySymbol(symbol);
	}

	public boolean existsBySymbol(String symbol) {
		return stockItemLogRepository.existsBySymbol(symbol);
	}

	public List<StockItemLog> findAll() {
		return stockItemLogRepository.findAll();
	}

	public List<StockItemLog> findByPriceDateBeforeOrIsNull(Date targetDate) {
		return stockItemLogRepository.findByPriceDateBeforeOrIsNull(targetDate);
	}

	public List<StockItemLog> findByStatsDateBeforeOrIsNull(Date targetDate) {
		return stockItemLogRepository.findByStatsDateBeforeOrIsNull(targetDate);
	}

	public Map<String, StockItemLog> findAllAsMap() {
		Map<String, StockItemLog> silMap = new TreeMap<>();
		List<StockItemLog> silList = findAll();
		silList.stream().forEach(sil -> silMap.put(sil.getSymbol(), sil));
		return silMap;
	}
}

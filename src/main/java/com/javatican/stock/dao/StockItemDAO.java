package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.StockItem;
import com.javatican.stock.repository.StockItemRepository;

@Repository("stockItemDAO")
public class StockItemDAO {
	public StockItemDAO() {
	}

	@Autowired
	StockItemRepository stockItemRepository;

	public StockItem save(StockItem si) {
		return stockItemRepository.save(si);
	}

	public Iterable<StockItem> saveAll(List<StockItem> siList) {
		return stockItemRepository.saveAll(siList);
	}

	public StockItem findBySymbol(String symbol) {
		return stockItemRepository.findBySymbol(symbol);
	}

	/*
	 * use entity graph load stbt relationship
	 */
	/*
	 * public StockItem getBySymbol(String symbol) { return
	 * stockItemRepository.getBySymbol(symbol); }
	 */
	public boolean existsBySymbol(String symbol) {
		return stockItemRepository.existsBySymbol(symbol);
	}

	public List<String> getAllSymbols() {
		return stockItemRepository.getAllSymbols();
	}

	public List<StockItem> findAll() {
		return stockItemRepository.findAll();
	}
//
//	public List<StockItem> getAllStockItems() {
//		return stockItemRepository.getAllStockItems();
//	}

	/*
	 * below is used for selecting price field value of 0.0
	 */
	public List<StockItem> findByPrice(Double price) {
		return stockItemRepository.findByPrice(price);
	}

	public Map<String, StockItem> findAllAsMap() {
		Map<String, StockItem> siMap = new TreeMap<>();
		//TODO check this.
		List<StockItem> siList = findAll();
		//List<StockItem> siList = getAllStockItems();
		siList.stream().forEach(si -> siMap.put(si.getSymbol(), si));
		return siMap;
	}
}

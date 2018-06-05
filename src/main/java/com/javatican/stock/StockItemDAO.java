package com.javatican.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.StockItem;

@Repository("stockItemDAO")
public class StockItemDAO {
	public StockItemDAO() {
		// TODO Auto-generated constructor stub
	}
	@Autowired
	StockItemRepository stockItemRepository;
		
	public void save(StockItem si){
		stockItemRepository.save(si);
	} 
	public void saveAll(List<StockItem> siList){
		stockItemRepository.saveAll(siList);
	} 
	public StockItem findBySymbol(String symbol) {
		return stockItemRepository.findBySymbol(symbol);
	}
	public boolean existsBySymbol(String symbol) {
		return stockItemRepository.existsBySymbol(symbol);
	}
	public List<String> getAllSymbols(){
		return stockItemRepository.getAllSymbols();
	}
	public List<StockItem> findAll(){
		return stockItemRepository.findAll();
	}

	public List<StockItem> findBySymbolIn(List<String> symbols){
		return stockItemRepository.findBySymbolIn(symbols);
	}
}

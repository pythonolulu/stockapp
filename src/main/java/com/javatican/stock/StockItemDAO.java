package com.javatican.stock;

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
		
	public void saveStockItem(StockItem si){
		stockItemRepository.save(si);
	} 
}

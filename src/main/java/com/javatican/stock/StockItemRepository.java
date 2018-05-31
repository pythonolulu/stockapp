package com.javatican.stock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.StockItem;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
	StockItem findBySymbol(String symbol);
	boolean existsBySymbol(String symbol);
	@Query("select s.symbol from StockItem s")
	List<String> getAllSymbols();
}

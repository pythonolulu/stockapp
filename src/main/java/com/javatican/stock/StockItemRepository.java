package com.javatican.stock;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import com.javatican.stock.model.StockItem;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
	StockItem findBySymbol(String symbol);
	
	@EntityGraph(value="StockItem.stbt", type=EntityGraphType.LOAD)
	StockItem getBySymbol(String symbol);
	
	boolean existsBySymbol(String symbol);
	
	@Query("select s.symbol from StockItem s")
	List<String> getAllSymbols();
	
	List<StockItem> findBySymbolIn(List<String> symbols);

	List<StockItem> findByPrice(Double price);
}

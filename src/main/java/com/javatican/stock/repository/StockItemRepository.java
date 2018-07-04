package com.javatican.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.StockItem;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
	StockItem findBySymbol(String symbol);

	boolean existsBySymbol(String symbol);

	List<StockItem> findByPrice(Double price);

	@Query("select s.symbol from StockItem s")
	List<String> getAllSymbols();
//
//	@Query("select s from StockItem s join s.stockItemLog t ")
//	List<StockItem> getAllStockItems();

	/*
	 * @EntityGraph(value = "StockItem.stbt", type = EntityGraphType.LOAD) StockItem
	 * getBySymbol(String symbol);
	 * 
	 * @EntityGraph(value = "StockItem.stbt", type = EntityGraphType.LOAD) StockItem
	 * findByIdIn(List<Long> ids);
	 * 
	 * @EntityGraph(value = "StockItem.stbt", type = EntityGraphType.LOAD)
	 * 
	 * @Query("select s from StockItem s join s.stbt t on t.tradingDate = ?1")
	 * List<StockItem> findByStbtTradingDate(Date date);
	 */
}

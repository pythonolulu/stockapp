package com.javatican.stock.repository;

import java.util.Date;
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

	List<StockItem> findByPriceDateBefore(Date targetDate);

	List<StockItem> findByStatsDateBefore(Date targetDate);

	List<StockItem> findByStatsDateIsNull();

	@Query("select s from StockItem s where s.priceDate is null or s.priceDate < ?1")
	List<StockItem> findByPriceDateBeforeOrIsNull(Date targetDate);

	@Query("select s from StockItem s where s.statsDate is null or s.statsDate < ?1")
	List<StockItem> findByStatsDateBeforeOrIsNull(Date targetDate);

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

package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemLog;

public interface StockItemLogRepository extends JpaRepository<StockItemLog, Long> {
	StockItemLog findBySymbol(String symbol);

	boolean existsBySymbol(String symbol);

	List<StockItemLog> findByPriceDateBefore(Date targetDate);

	List<StockItemLog> findByStatsDateBefore(Date targetDate);

	List<StockItemLog> findByStatsDateIsNull();

	@Query("select s from StockItemLog s where s.valid=true and (s.priceDate is null or s.priceDate < ?1)")
	List<StockItemLog> findByPriceDateBeforeOrIsNull(Date targetDate);

	@Query("select s from StockItemLog s where s.valid=true and (s.statsDate is null or s.statsDate < ?1)")
	List<StockItemLog> findByStatsDateBeforeOrIsNull(Date targetDate);

	@Query("select s from StockItemLog s where s.valid=true")
	List<StockItemLog> findAll();
}

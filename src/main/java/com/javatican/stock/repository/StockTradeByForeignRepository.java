package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.StockTradeByTrust;

public interface StockTradeByForeignRepository extends JpaRepository<StockTradeByForeign, Long> {

	@Query("select max(s.tradingDate) from StockTradeByForeign s")
	Date getLatestTradingDate();

	@Query("select distinct(s.stockSymbol) from StockTradeByForeign s")
	List<String> getDistinctStockSymbol();

	/*
	 * below will select StockTradeByForeign records on the specified date with
	 * stockItem relationship and stockItem.stbf relationship pre-selected.
	 */
	@EntityGraph(value = "StockTradeByForeign.stockItem.stbf", type = EntityGraphType.LOAD)
	List<StockTradeByForeign> findByTradingDate(Date tradingDate);

	List<StockTradeByForeign> findByStockSymbolOrderByTradingDateAsc(String stockSymbol);
}

package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.StockTradeByTrust;

public interface StockTradeByTrustRepository extends JpaRepository<StockTradeByTrust, Long> {

	@Query("select max(s.tradingDate) from StockTradeByTrust s")
	Date getLatestTradingDate();

	@Query("select distinct(s.stockSymbol) from StockTradeByTrust s")
	List<String> getDistinctStockSymbol();

	/*
	 * below will select StockTradeByTrust records on the specified date with
	 * stockItem relationship and stockItem.stbt relationship pre-selected.
	 */
	@EntityGraph(value = "StockTradeByTrust.stockItem.stbt", type = EntityGraphType.LOAD)
	List<StockTradeByTrust> findByTradingDate(Date tradingDate);

}

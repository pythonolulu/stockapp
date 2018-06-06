package com.javatican.stock;

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
	
	@EntityGraph(value="StockTradeByTrust.stockItem", type=EntityGraphType.LOAD)
	List<StockTradeByTrust> findByTradingDate(Date tradingDate);
	
	//List<StockTradeByTrust> findByStockSymbol(String stockSymbol);
	
}

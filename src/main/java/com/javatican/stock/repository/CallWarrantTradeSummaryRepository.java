package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
 
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.StockItem;

public interface CallWarrantTradeSummaryRepository extends JpaRepository<CallWarrantTradeSummary, Long> {
	
	List<CallWarrantTradeSummary> findByTradingDate(Date tradingDate);
	
	List<CallWarrantTradeSummary> findByStockSymbolOrderByTradingDateAsc(String stockSymbol);

	@Query("select max(s.tradingDate) from CallWarrantTradeSummary s")
	Date getLatestTradingDate();
	
	@Query("select s.stockSymbol from CallWarrantTradeSummary s where s.tradingDate = ( select max(s.tradingDate) from CallWarrantTradeSummary s) ")
	List<String> getStockSymbolsWithCallWarrant();
	
	@Query("select s.stockItem from CallWarrantTradeSummary s where s.tradingDate = ( select max(s.tradingDate) from CallWarrantTradeSummary s) ")
	List<StockItem> getStockItemsWithCallWarrant();
}

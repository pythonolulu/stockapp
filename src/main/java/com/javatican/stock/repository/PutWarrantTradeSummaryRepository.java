package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.PutWarrantTradeSummary;
import com.javatican.stock.model.StockItem;

public interface PutWarrantTradeSummaryRepository extends JpaRepository<PutWarrantTradeSummary, Long> {
	
	List<PutWarrantTradeSummary> findByTradingDate(Date tradingDate);

	List<PutWarrantTradeSummary> findByStockSymbolOrderByTradingDateAsc(String stockSymbol);
	
	@Query("select max(s.tradingDate) from PutWarrantTradeSummary s")
	Date getLatestTradingDate();

	@Query("select s.stockSymbol from PutWarrantTradeSummary s where s.tradingDate = ( select max(s.tradingDate) from PutWarrantTradeSummary s) ")
	List<String> getStockSymbolsWithPutWarrant();

	@Query("select t from PutWarrantTradeSummary s join s.stockItem t where s.tradingDate = ( select max(s.tradingDate) from PutWarrantTradeSummary s) ")
	List<StockItem> getStockItemsWithPutWarrant();
}

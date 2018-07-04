package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
  
import com.javatican.stock.model.DealerTradeSummary; 

public interface DealerTradeSummaryRepository extends JpaRepository<DealerTradeSummary, Long> {
	
	List<DealerTradeSummary> findByTradingDate(Date tradingDate);
	
	List<DealerTradeSummary> findByStockSymbolOrderByTradingDateAsc(String stockSymbol);

	@Query("select max(s.tradingDate) from DealerTradeSummary s")
	Date getLatestTradingDate();
}

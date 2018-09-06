package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
 
import com.javatican.stock.model.OptionSeriesData;

public interface OptionSeriesDataRepository extends JpaRepository<OptionSeriesData, Long> {
	boolean existsByTradingDate(Date tradingDate);

	OptionSeriesData getByTradingDate(Date tradingDate);

	List<OptionSeriesData> findAllByOrderByTradingDate();

	@Query("select max(s.tradingDate) from OptionSeriesData s")
	Date getLatestTradingDate();
}

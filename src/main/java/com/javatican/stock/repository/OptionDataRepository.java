package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
 
import com.javatican.stock.model.OptionData;

public interface OptionDataRepository extends JpaRepository<OptionData, Long> {
	boolean existsByTradingDate(Date tradingDate);

	OptionData getByTradingDate(Date tradingDate);

	List<OptionData> findAllByOrderByTradingDate();

	@Query("select max(s.tradingDate) from OptionData s")
	Date getLatestTradingDate();
}

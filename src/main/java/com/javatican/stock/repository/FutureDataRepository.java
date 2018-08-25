package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.FutureData;

public interface FutureDataRepository extends JpaRepository<FutureData, Long> {
	boolean existsByTradingDate(Date tradingDate);

	FutureData getByTradingDate(Date tradingDate);

	List<FutureData> findAllByOrderByTradingDate();
}

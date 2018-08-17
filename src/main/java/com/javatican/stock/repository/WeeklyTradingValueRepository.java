package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.WeeklyTradingValue;

public interface WeeklyTradingValueRepository extends JpaRepository<WeeklyTradingValue, Long> {
	boolean existsByTradingDate(Date tradingDate);

	WeeklyTradingValue getByTradingDate(Date tradingDate);

	List<WeeklyTradingValue> findAllByOrderByTradingDate();
}

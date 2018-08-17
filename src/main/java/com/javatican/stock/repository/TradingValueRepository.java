package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.TradingValue;

public interface TradingValueRepository extends JpaRepository<TradingValue, Long> {
	boolean existsByTradingDate(Date tradingDate);

	TradingValue getByTradingDate(Date tradingDate);

	List<TradingValue> findAllByOrderByTradingDate();
}

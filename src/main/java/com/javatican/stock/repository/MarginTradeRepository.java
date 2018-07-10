package com.javatican.stock.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.MarginTrade; 

public interface MarginTradeRepository extends JpaRepository<MarginTrade, Long> {
	boolean existsByTradingDate(Date tradingDate);
}

package com.javatican.stock;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.TradingDate;

public interface TradingDateRepository extends JpaRepository<TradingDate, Long> {
	TradingDate findTopByOrderByDateDesc();
	boolean existsByDate(Date date);
}

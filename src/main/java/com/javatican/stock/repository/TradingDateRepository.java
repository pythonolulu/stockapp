package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.TradingDate;

public interface TradingDateRepository extends JpaRepository<TradingDate, Long> {
	TradingDate findTopByOrderByDateDesc();

	boolean existsByDate(Date date);

	List<TradingDate> findByDateBetween(Date begin, Date end);

	List<TradingDate> findByDateAfter(Date date);
}

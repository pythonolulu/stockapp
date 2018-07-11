package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.TradingDate;

public interface TradingDateRepository extends JpaRepository<TradingDate, Long> {
	TradingDate findTopByOrderByDateDesc();
	
	@Query("select t.date from TradingDate t order by t.date desc ")
	List<Date> findLatestNTradingDateDesc(Pageable pageable);

	boolean existsByDate(Date date);

	List<TradingDate> findByDateBetween(Date begin, Date end);

	List<TradingDate> findByDateAfter(Date date);
}

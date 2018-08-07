package com.javatican.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.WatchLog;

public interface WatchLogRepository extends JpaRepository<WatchLog, Long> {
	
	
}

package com.javatican.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.StockItem;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
	StockItem findBySymbol(String symbol);
}

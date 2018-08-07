package com.javatican.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.SiteUser;
import com.javatican.stock.model.WatchItem;

public interface WatchItemRepository extends JpaRepository<WatchItem, Long> {
	
	@EntityGraph(value = "WatchItem.wl", type = EntityGraphType.LOAD)
	WatchItem getByIdAndSiteUser(Long id, SiteUser su);

	boolean existsBySymbolAndSiteUser(String symbol, SiteUser su);
	
	WatchItem findBySymbolAndSiteUser(String symbol, SiteUser su);
	/*
	 * getBySymbolAndSiteUser() also fetch WatchLog and StockItem relationships
	 */
	@EntityGraph(value = "WatchItem.wl_stockItem", type = EntityGraphType.LOAD)
	WatchItem getBySymbolAndSiteUser(String symbol, SiteUser su);

	@Query("select s from WatchItem s where s.siteUser=?1 order by s.symbol")
	List<WatchItem> findBySiteUserOrderBySymbol(SiteUser su);

	/*
	 * getBySiteUserOrderBySymbol() also fetch WatchLog and StockItem relationships
	 */
	@EntityGraph(value = "WatchItem.wl_stockItem", type = EntityGraphType.LOAD)
	@Query("select s from WatchItem s where s.siteUser=?1 order by s.symbol")
	List<WatchItem> getBySiteUserOrderBySymbol(SiteUser su);

	@Query("select s.symbol from WatchItem s where s.siteUser=?1 order by s.symbol")
	List<String> getSymbolsBySiteUser(SiteUser su);
}

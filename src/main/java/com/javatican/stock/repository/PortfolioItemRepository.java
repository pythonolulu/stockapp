package com.javatican.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.SiteUser;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
	
	/*
	 * get the stockItem relationship 
	 */
	@EntityGraph(value = "PortfolioItem.stockItem", type = EntityGraphType.LOAD)
	PortfolioItem getByIdAndSiteUser(Long id, SiteUser su);
	
	List<PortfolioItem> findBySymbolAndSiteUser(String symbol, SiteUser su);

	/*
	 * get the stockItem relationship 
	 */
	@EntityGraph(value = "PortfolioItem.stockItem", type = EntityGraphType.LOAD)
	@Query("select s from PortfolioItem s where s.siteUser=?1 order by s.isClosed, s.symbol")
	List<PortfolioItem> findBySiteUserOrderBySymbol(SiteUser su);

	@Query("select distinct s.symbol from PortfolioItem s where s.siteUser=?1 order by s.symbol")
	List<String> getDistinctSymbolsBySiteUser(SiteUser su);

}

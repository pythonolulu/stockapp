package com.javatican.stock.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.repository.PortfolioItemRepository;

@Repository("PortfolioItemDAO")
public class PortfolioItemDAO {
	public PortfolioItemDAO() {
	}

	@Autowired
	PortfolioItemRepository portfolioItemRepository;

	public PortfolioItem save(PortfolioItem pi) {
		return portfolioItemRepository.save(pi);
	}

	public Iterable<PortfolioItem> saveAll(List<PortfolioItem> piList) {
		return portfolioItemRepository.saveAll(piList);
	}
	/*
	 * get the stockItem relationship 
	 */
	public PortfolioItem getByIdAndSiteUser(Long id, SiteUser su) {
		return portfolioItemRepository.getByIdAndSiteUser(id, su);
	}

	public List<PortfolioItem> findBySymbolAndSiteUser(String symbol, SiteUser su) {
		return portfolioItemRepository.findBySymbolAndSiteUser(symbol, su);
	}

	public List<String> getDistinctSymbolsBySiteUser(SiteUser su) {
		return portfolioItemRepository.getDistinctSymbolsBySiteUser(su);
	}

	/*
	 * get the stockItem relationship 
	 */
	public List<PortfolioItem> findBySiteUser(SiteUser su) {
		return portfolioItemRepository.findBySiteUserOrderBySymbol(su);
	}
}

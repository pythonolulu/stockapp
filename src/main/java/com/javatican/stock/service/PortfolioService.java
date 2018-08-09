package com.javatican.stock.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.PortfolioItemDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.WatchItem;

@Service("portfolioService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class PortfolioService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	PortfolioItemDAO portfolioItemDAO;

	public PortfolioService() {
	}

	public PortfolioItem save(PortfolioItem pi) {
		return portfolioItemDAO.save(pi);
	}

	public void delete(PortfolioItem pi) {
		portfolioItemDAO.delete(pi);
	}
	public Iterable<PortfolioItem> saveAll(List<PortfolioItem> piList) {
		return portfolioItemDAO.saveAll(piList);
	}
	/*
	 * get the stockItem relationship 
	 */
	public PortfolioItem getByIdAndSiteUser(Long id, SiteUser su) {
		return portfolioItemDAO.getByIdAndSiteUser(id, su);
	}

	public List<PortfolioItem> findBySymbolAndSiteUser(String symbol, SiteUser su) {
		return portfolioItemDAO.findBySymbolAndSiteUser(symbol, su);
	}

	public List<String> getDistinctSymbolsBySiteUser(SiteUser su) {
		return portfolioItemDAO.getDistinctSymbolsBySiteUser(su);
	}

	/*
	 * get the stockItem relationship 
	 */
	public List<PortfolioItem> findBySiteUser(SiteUser su) {
		return portfolioItemDAO.findBySiteUser(su);
	}

	public Map<StockItem, PortfolioItem> findBySiteUserAsMap(SiteUser su) {
		List<PortfolioItem> piList = portfolioItemDAO.findBySiteUser(su);
		Map<StockItem, PortfolioItem> piMap = new HashMap<>();
		piList.stream().forEach(pi -> piMap.put(pi.getStockItem(), pi));
		return piMap;
	}
}
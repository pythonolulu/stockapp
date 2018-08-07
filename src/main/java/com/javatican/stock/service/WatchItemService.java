package com.javatican.stock.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.WatchItemDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.model.WatchItem;
import com.javatican.stock.model.WatchLog;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.model.StockItem;

@Service("watchItemService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class WatchItemService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	WatchItemDAO watchItemDAO;

	public WatchItemService() {
	}

	public WatchItem save(WatchItem wi) {
		return watchItemDAO.save(wi);
	}
	public WatchLog saveWatchLog(WatchLog wi) {
		return watchItemDAO.saveWatchLog(wi);
	}

	public Iterable<WatchItem> saveAll(List<WatchItem> wiList) {
		return watchItemDAO.saveAll(wiList);
	}

	public void delete(WatchItem wi) {
		watchItemDAO.delete(wi);
	}

	public WatchItem getByIdAndSiteUser(Long id, SiteUser su) {
		return watchItemDAO.getByIdAndSiteUser(id, su);
	}

	public WatchItem findBySymbolAndSiteUser(String symbol, SiteUser su) {
		return watchItemDAO.findBySymbolAndSiteUser(symbol, su);
	}
	
	public boolean existsBySymbolAndSiteUser(String symbol, SiteUser su) {
		return watchItemDAO.existsBySymbolAndSiteUser(symbol, su);
	}

	/*
	 * findBySymbolAndSiteUser2(): also retrieve WatchLog/StockItem relationships
	 */
	public WatchItem findBySymbolAndSiteUser2(String symbol, SiteUser su) {
		return watchItemDAO.findBySymbolAndSiteUser2(symbol, su);
	}

	public List<String> getSymbolsBySiteUser(SiteUser su) {
		return watchItemDAO.getSymbolsBySiteUser(su);
	}

	public List<WatchItem> findBySiteUser(SiteUser su) {
		return watchItemDAO.findBySiteUser(su);
	}

	/*
	 * findBySiteUser2(): also retrieve WatchLog/StockItem relationships
	 */
	public List<WatchItem> findBySiteUser2(SiteUser su) {
		return watchItemDAO.findBySiteUser2(su);
	}

	public TreeMap<StockItem, WatchItem> findBySiteUserAsMap(SiteUser su) {
		List<WatchItem> wiList = watchItemDAO.findBySiteUser(su);
		TreeMap<StockItem, WatchItem> wiMap = new TreeMap<>();
		wiList.stream().forEach(wi -> wiMap.put(wi.getStockItem(), wi));
		return wiMap;
	}
	/*
	 * findBySiteUser2AsMap(): also retrieve WatchLog/StockItem relationships
	 */
	public TreeMap<StockItem, WatchItem> findBySiteUser2AsMap(SiteUser su) {
		List<WatchItem> wiList = watchItemDAO.findBySiteUser2(su);
		TreeMap<StockItem, WatchItem> wiMap = new TreeMap<>();
		wiList.stream().forEach(wi -> wiMap.put(wi.getStockItem(), wi));
		return wiMap;
	}
}
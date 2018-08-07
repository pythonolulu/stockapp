package com.javatican.stock.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.WatchItem;
import com.javatican.stock.model.WatchLog;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.repository.WatchItemRepository;
import com.javatican.stock.repository.WatchLogRepository;

@Repository("watchItemDAO")
public class WatchItemDAO {
	public WatchItemDAO() {
	}

	@Autowired
	WatchItemRepository watchItemRepository;
	@Autowired
	WatchLogRepository watchLogRepository;

	public WatchItem save(WatchItem wi) {
		return watchItemRepository.save(wi);
	}

	public WatchLog saveWatchLog(WatchLog wl) {
		return watchLogRepository.save(wl);
	}

	public void delete(WatchItem wi) {
		watchItemRepository.delete(wi);
	}

	public Iterable<WatchItem> saveAll(List<WatchItem> wiList) {
		return watchItemRepository.saveAll(wiList);
	}

	public WatchItem getByIdAndSiteUser(Long id, SiteUser su) {
		return watchItemRepository.getByIdAndSiteUser(id, su);
	}

	public boolean existsBySymbolAndSiteUser(String symbol, SiteUser su) {
		return watchItemRepository.existsBySymbolAndSiteUser(symbol, su);
	}

	public WatchItem findBySymbolAndSiteUser(String symbol, SiteUser su) {
		return watchItemRepository.findBySymbolAndSiteUser(symbol, su);
	}

	/*
	 * findBySymbolAndSiteUser2(): also retrieve WatchLog/StockItem relationships
	 */
	public WatchItem findBySymbolAndSiteUser2(String symbol, SiteUser su) {
		return watchItemRepository.getBySymbolAndSiteUser(symbol, su);
	}

	public List<String> getSymbolsBySiteUser(SiteUser su) {
		return watchItemRepository.getSymbolsBySiteUser(su);
	}

	public List<WatchItem> findBySiteUser(SiteUser su) {
		return watchItemRepository.findBySiteUserOrderBySymbol(su);
	}

	/*
	 * findBySiteUser2(): also retrieve WatchLog/StockItem relationships
	 */
	public List<WatchItem> findBySiteUser2(SiteUser su) {
		return watchItemRepository.getBySiteUserOrderBySymbol(su);
	}
}

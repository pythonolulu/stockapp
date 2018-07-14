package com.javatican.stock.service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.CallWarrantSelectStrategy1DAO;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.WarrantTradeDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.WarrantTrade;
import com.javatican.stock.util.StockUtils;

@Service("strategyService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StrategyService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;
	@Autowired
	WarrantTradeDAO warrantTradeDAO;
	@Autowired
	CallWarrantSelectStrategy1DAO callWarrantSelectStrategy1DAO;

	/*
	 * Buy a call warrant(with the largest trade value for the specific target
	 * stock) on each trading date and hold for a 'holdPeriod' day period, calculate
	 * the probability of going up and sort the stock targets based on this
	 * probability. Parameters: holdPeriod: the period of each warrant item to hold;
	 * dataDatePeriod: how many days of data to calculate
	 * 
	 */

	public Map<String, Map<String, Double>> callWarrantSelectStrategy1(String dateString, int holdPeriod)
			throws StockException {
		if (callWarrantSelectStrategy1DAO.existsForCombinedResult(dateString, holdPeriod)) {
			return callWarrantSelectStrategy1DAO.loadCombinedResult(dateString, holdPeriod);
		}
		// get the latest 'dataDatePeriod' trading dates.
		List<Date> dateList = tradingDateDAO.findAllTradingDate();
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		// statsMap: the map to store the calculated probability for warrants to go up
		// for each stock symbol and for each trading date
		// Key is the symbol. Value is a map object with key 'dateString' and
		// value 'up percentage'
		Map<String, Map<String, Double>> statsMap = new TreeMap<>();
		// cachedWTMap : to speed up loading , some loaded warrant trade data will be
		// cached.
		// Key is the dateString. Value is a map object with key
		// 'warrant symbol' and value 'the WarrantTrade object'
		Map<String, Map<String, WarrantTrade>> cachedWTMap = new TreeMap<>();
		for (Date d : dateList) {
			String dString = StockUtils.dateToSimpleString(d);
			Map<String, WarrantTrade> wtMap = null;
			if ((wtMap = cachedWTMap.get(dString)) == null) {
				wtMap = warrantTradeDAO.loadCallWarrantsAsMap(dString);
			} else {
				cachedWTMap.remove(dString);
			}
			// get the trading date which is of 'holdPeriod' days after the current date
			String dateStringNext = StockUtils.getNextNTradingDate(dString, holdPeriod, dateList);
			if (dateStringNext == null)
				break;
			Map<String, WarrantTrade> wtMapNext = warrantTradeDAO.loadCallWarrantsAsMap(dateStringNext);
			// store in the cached map
			cachedWTMap.put(dateStringNext, wtMapNext);
			//
			for (String symbol : symbolList) {
				// get the largest trade value of warrant for the symbol
				Optional<WarrantTrade> targetWTOp = wtMap.values().stream()
						.filter(wt -> wt.getStockSymbol().equals(symbol))
						.sorted(Comparator.comparing(WarrantTrade::getTradeValue).reversed()).findFirst();
				if (!targetWTOp.isPresent()) {
					continue;
				}
				WarrantTrade targetWT = targetWTOp.get();
				WarrantTrade targetWTNext = wtMapNext.get(targetWT.getWarrantSymbol());
				if (targetWTNext == null) {
					logger.info("No corresponding trade data for warrant: " + targetWT.getWarrantSymbol()
							+ " of target symbol: " + symbol + " and date: " + dateStringNext);
					continue;
				}
				//
				double upPercent = StockUtils
						.roundDoubleDp4((targetWTNext.getAvgPrice() - targetWT.getAvgPrice()) / targetWT.getAvgPrice());
				Map<String, Double> upPercentMap;
				if ((upPercentMap = statsMap.get(symbol)) == null) {
					upPercentMap = new TreeMap<>();
					statsMap.put(symbol, upPercentMap);
				}
				upPercentMap.put(dString, upPercent);
			}
		}
		// write upPercentMap to file for each stock item
		for (String symbol : statsMap.keySet()) {
			Map<String, Double> upPercentMap = statsMap.get(symbol);
			callWarrantSelectStrategy1DAO.save(symbol,holdPeriod, upPercentMap);
		}
		// write combined result
		callWarrantSelectStrategy1DAO.saveCombinedResult(dateString, holdPeriod, statsMap);
		return statsMap;
	}

	public void putWarrantSelectStrategy1(int i, int tradingDateCount) {
		// TODO Auto-generated method stub

	}

}
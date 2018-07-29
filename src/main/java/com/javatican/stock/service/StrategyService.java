package com.javatican.stock.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.CallWarrantSelectStrategy1DAO;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.PutWarrantSelectStrategy1DAO;
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.WarrantTradeDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
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
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	StockItemWeeklyDataDAO stockItemWeeklyDataDAO;
	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;
	@Autowired
	PutWarrantTradeSummaryDAO putWarrantTradeSummaryDAO;
	@Autowired
	WarrantTradeDAO warrantTradeDAO;
	@Autowired
	CallWarrantSelectStrategy1DAO callWarrantSelectStrategy1DAO;
	@Autowired
	PutWarrantSelectStrategy1DAO putWarrantSelectStrategy1DAO;

	public Map<String, List<Number>> getStatsDataCallW(String dateString, int holdPeriod, int dataDatePeriod) {
		if (callWarrantSelectStrategy1DAO.statsDataExistsFor(dateString, holdPeriod, dataDatePeriod)) {
			try {
				return callWarrantSelectStrategy1DAO.loadStatsData(dateString, holdPeriod, dataDatePeriod);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 1 of date:" + dateString + ", holdPeriod:"
						+ holdPeriod + ", dataDatePeriod:" + dataDatePeriod);
				return null;
			}
		} else {
			Date date = tradingDateDAO.getLatestTradingDate();
			// all the stock items with call warrants(used for getting its item name)
			Map<String, StockItem> siMap = callWarrantTradeSummaryDAO.getStockItemsWithCallWarrant();
			Map<String, List<Number>> statsMap = new HashMap<>();
			for (String stockSymbol : siMap.keySet()) {
				TreeMap<String, Double> rawStatsData;
				try {
					rawStatsData = callWarrantSelectStrategy1DAO.loadRawStatsData(stockSymbol, holdPeriod);
				} catch (StockException e1) {
					logger.warn("No raw stats data for symbol:" + stockSymbol);
					continue;
				}
				// filteredMap: store filtered result of rawStatsData (only select the
				// latest 'dataDatePeriod' trading dates)
				final TreeMap<String, Double> filteredMap;
				if (rawStatsData.size() > dataDatePeriod) {
					filteredMap = new TreeMap<>();
					// below is not compiling because collect(Collectors.toMap()) method will
					// return a Map object, can not be casted to a TreeMap
					//
					// filteredMap =
					// rawStatsData.entrySet().stream().skip(rawStatsData.size() - dataDatePeriod)
					// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					rawStatsData.entrySet().stream().skip(rawStatsData.size() - dataDatePeriod)
							.forEach(e -> filteredMap.put(e.getKey(), e.getValue()));
				} else {
					filteredMap = rawStatsData;
				}
				long size = filteredMap.size();
				if (size == 0)
					continue;
				long up_size = filteredMap.values().stream().filter(value -> value > 0.0).count();
				double acc_percent = filteredMap.values().stream().reduce(0.0, (a, b) -> a + b);
				// value is a List<Number>: store data size, up ratio, accumulated percentage
				statsMap.put(stockSymbol, Arrays.asList(size, StockUtils.roundDoubleDp4((double) up_size / size),
						StockUtils.roundDoubleDp4(acc_percent)));
			}
			try {
				callWarrantSelectStrategy1DAO.saveStatsData(dateString, holdPeriod, dataDatePeriod, statsMap);
			} catch (StockException e) {
				e.printStackTrace();
				logger.warn("Error saving stats data of date:" + dateString + ", holdPeriod:" + holdPeriod
						+ ", dataDatePeriod:" + dataDatePeriod);
			}
			return statsMap;
		}

	}
  

	public Map<String, List<Number>> getStatsDataPutW(String dateString, int holdPeriod, int dataDatePeriod) {
		if (putWarrantSelectStrategy1DAO.statsDataExistsFor(dateString, holdPeriod, dataDatePeriod)) {
			try {
				return putWarrantSelectStrategy1DAO.loadStatsData(dateString, holdPeriod, dataDatePeriod);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 1 of date:" + dateString + ", holdPeriod:"
						+ holdPeriod + ", dataDatePeriod:" + dataDatePeriod);
				return null;
			}
		} else {
			Date date = tradingDateDAO.getLatestTradingDate();
			// all the stock items with put warrants(used for getting its item name)
			Map<String, StockItem> siMap = putWarrantTradeSummaryDAO.getStockItemsWithPutWarrant();
			Map<String, List<Number>> statsMap = new HashMap<>();
			for (String stockSymbol : siMap.keySet()) {
				TreeMap<String, Double> rawStatsData;
				try {
					rawStatsData = putWarrantSelectStrategy1DAO.loadRawStatsData(stockSymbol, holdPeriod);
				} catch (StockException e1) {
					logger.warn("No raw stats data for symbol:" + stockSymbol);
					continue;
				}
				// filteredMap: store filtered result of rawStatsData (only select the
				// latest 'dataDatePeriod' trading dates)
				final TreeMap<String, Double> filteredMap;
				if (rawStatsData.size() > dataDatePeriod) {
					filteredMap = new TreeMap<>();
					// below is not compiling because collect(Collectors.toMap()) method will
					// return a Map object, can not be casted to a TreeMap
					//
					// filteredMap =
					// rawStatsData.entrySet().stream().skip(rawStatsData.size() - dataDatePeriod)
					// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					rawStatsData.entrySet().stream().skip(rawStatsData.size() - dataDatePeriod)
							.forEach(e -> filteredMap.put(e.getKey(), e.getValue()));
				} else {
					filteredMap = rawStatsData;
				}
				long size = filteredMap.size();
				if (size == 0)
					continue;
				long up_size = filteredMap.values().stream().filter(value -> value > 0.0).count();
				double acc_percent = filteredMap.values().stream().reduce(0.0, (a, b) -> a + b);
				// value is a List<Number>: store size, up_size/size, acc_percent
				statsMap.put(stockSymbol, Arrays.asList(size, StockUtils.roundDoubleDp4((double) up_size / size),
						StockUtils.roundDoubleDp4(acc_percent)));
			}
			try {
				putWarrantSelectStrategy1DAO.saveStatsData(dateString, holdPeriod, dataDatePeriod, statsMap);
			} catch (StockException e) {
				e.printStackTrace();
				logger.warn("Error saving stats data of date:" + dateString + ", holdPeriod:" + holdPeriod
						+ ", dataDatePeriod:" + dataDatePeriod);
			}
			return statsMap;
		}

	}
	/*
	 * Buy a call warrant(with the largest trade value for the specific target
	 * stock) on each trading date and hold for a 'holdPeriod' day period, calculate
	 * the percentage that it goes up(or negative for going down)
	 */
	public void prepareRawStatsDataForCallW(int holdPeriod) throws StockException {
		// get all trading dates.
		List<Date> dateList = tradingDateDAO.findAllTradingDate();
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		// statsMap: the map to store the calculated percentage for warrants to go up
		// for each stock symbol and for each trading date
		// Key is the symbol. Value is a tree map object with key 'dateString' and
		// value 'up percentage'
		Map<String, TreeMap<String, Double>> statsMap = new HashMap<>();
		// cachedWTMap : to speed up loading , some loaded warrant trade data will be
		// cached.
		// Key is the dateString. Value is a map object with key
		// 'warrant symbol' and value 'the WarrantTrade object'
		TreeMap<String, Map<String, WarrantTrade>> cachedWTMap = new TreeMap<>();
		for (Date d : dateList) {
			String dString = StockUtils.dateToSimpleString(d);
			// key:'warrant symbol' and value: 'the WarrantTrade object'
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
				TreeMap<String, Double> rawStatsMap;
				if ((rawStatsMap = statsMap.get(symbol)) == null) {
					rawStatsMap = new TreeMap<>();
					statsMap.put(symbol, rawStatsMap);
				}
				rawStatsMap.put(dString, upPercent);
			}
		}
		// write to file for each stock item
		for (String symbol : statsMap.keySet()) {
			callWarrantSelectStrategy1DAO.saveRawStatsData(symbol, holdPeriod, statsMap.get(symbol));
		}
	}

	public void prepareRawStatsDataForPutW(int holdPeriod) throws StockException {
		// get all trading dates.
		List<Date> dateList = tradingDateDAO.findAllTradingDate();
		// get all symbols that have call warrants
		List<String> symbolList = putWarrantTradeSummaryDAO.getStockSymbolsWithPutWarrant();
		// statsMap: the map to store the calculated percentage for warrants to go up
		// for each stock symbol and for each trading date
		// Key is the symbol. Value is a tree map object with key 'dateString' and
		// value 'up percentage'
		Map<String, TreeMap<String, Double>> statsMap = new HashMap<>();
		// cachedWTMap : to speed up loading , some loaded warrant trade data will be
		// cached.
		// Key is the dateString. Value is a map object with key
		// 'warrant symbol' and value 'the WarrantTrade object'
		TreeMap<String, Map<String, WarrantTrade>> cachedWTMap = new TreeMap<>();
		for (Date d : dateList) {
			String dString = StockUtils.dateToSimpleString(d);
			Map<String, WarrantTrade> wtMap = null;
			if ((wtMap = cachedWTMap.get(dString)) == null) {
				wtMap = warrantTradeDAO.loadPutWarrantsAsMap(dString);
			} else {
				cachedWTMap.remove(dString);
			}
			// get the trading date which is of 'holdPeriod' days after the current date
			String dateStringNext = StockUtils.getNextNTradingDate(dString, holdPeriod, dateList);
			if (dateStringNext == null)
				break;
			Map<String, WarrantTrade> wtMapNext = warrantTradeDAO.loadPutWarrantsAsMap(dateStringNext);
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
				TreeMap<String, Double> upPercentMap;
				if ((upPercentMap = statsMap.get(symbol)) == null) {
					upPercentMap = new TreeMap<>();
					statsMap.put(symbol, upPercentMap);
				}
				upPercentMap.put(dString, upPercent);
			}
		}
		// write to file for each stock item
		for (String symbol : statsMap.keySet()) {
			TreeMap<String, Double> upPercentMap = statsMap.get(symbol);
			putWarrantSelectStrategy1DAO.saveRawStatsData(symbol, holdPeriod, upPercentMap);
		}
	}

}
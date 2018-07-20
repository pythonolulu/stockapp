package com.javatican.stock.service;

import java.util.ArrayList;
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

import com.javatican.stock.StockException;
import com.javatican.stock.dao.CallWarrantSelectStrategy1DAO;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.PriceBreakUpSelectStrategy3DAO;
import com.javatican.stock.dao.PriceBreakUpSelectStrategy4DAO;
import com.javatican.stock.dao.PutWarrantSelectStrategy1DAO;
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.dao.Sma20SelectStrategy2DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.WarrantTradeDAO;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockItemWeeklyData;
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
	@Autowired
	Sma20SelectStrategy2DAO sma20SelectStrategy2DAO;
	@Autowired
	PriceBreakUpSelectStrategy3DAO priceBreakUpSelectStrategy3DAO;
	@Autowired
	PriceBreakUpSelectStrategy4DAO priceBreakUpSelectStrategy4DAO;

	public Map<String, TreeMap<String, Double>> getStatsDataForCallWarrantSelectStrategy1(int holdPeriod) {
		if (callWarrantSelectStrategy1DAO.existsForCombinedResult(holdPeriod)) {
			try {
				return callWarrantSelectStrategy1DAO.loadCombinedResult(holdPeriod);
			} catch (StockException e) {
				logger.warn("Error loading StatsData for callWarrantSelectStrategy1 of holdPeriod:" + holdPeriod);
				return null;
			}
		} else {
			return null;
		}
	}

	public Map<String, TreeMap<String, Double>> getStatsDataForPutWarrantSelectStrategy1(int holdPeriod) {
		if (putWarrantSelectStrategy1DAO.existsForCombinedResult(holdPeriod)) {
			try {
				return putWarrantSelectStrategy1DAO.loadCombinedResult(holdPeriod);
			} catch (StockException e) {
				logger.warn("Error loading StatsData for putWarrantSelectStrategy1 of holdPeriod:" + holdPeriod);
				return null;
			}
		} else {
			return null;
		}
	}

	/*
	 * Buy a call warrant(with the largest trade value for the specific target
	 * stock) on each trading date and hold for a 'holdPeriod' day period, calculate
	 * the percentage that it goes up(or negative for going down)
	 */
	public void prepareDataForCallWarrantSelectStrategy1(int holdPeriod) throws StockException {
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
				TreeMap<String, Double> upPercentMap;
				if ((upPercentMap = statsMap.get(symbol)) == null) {
					upPercentMap = new TreeMap<>();
					statsMap.put(symbol, upPercentMap);
				}
				upPercentMap.put(dString, upPercent);
			}
		}
		// write upPercentMap to file for each stock item
		for (String symbol : statsMap.keySet()) {
			TreeMap<String, Double> upPercentMap = statsMap.get(symbol);
			callWarrantSelectStrategy1DAO.save(symbol, holdPeriod, upPercentMap);
		}
		// write combined result
		callWarrantSelectStrategy1DAO.saveCombinedResult(holdPeriod, statsMap);
	}

	public void prepareDataForPutWarrantSelectStrategy1(int holdPeriod) throws StockException {
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
		// write upPercentMap to file for each stock item
		for (String symbol : statsMap.keySet()) {
			TreeMap<String, Double> upPercentMap = statsMap.get(symbol);
			putWarrantSelectStrategy1DAO.save(symbol, holdPeriod, upPercentMap);
		}
		// write combined result
		putWarrantSelectStrategy1DAO.saveCombinedResult(holdPeriod, statsMap);
	}

	public int getDaysAboveSma20(List<StockItemData> sidList) {
		int count = 0;
		for (int i = sidList.size() - 1; i >= 0; i--) {
			StockItemData sid = sidList.get(i);
			if (sid.getStockPrice().getClose() >= sid.getSma20()) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	public boolean isLatestPriceAboveSma20(List<StockItemData> sidList) {
		StockItemData latest = sidList.get(sidList.size() - 1);
		return latest.getStockPrice().getClose() >= latest.getSma20();
	}

	public int getDaysSma20GoingUp(List<StockItemData> sidList) {
		int count = 0;
		for (int i = sidList.size() - 1; i >= 1; i--) {
			StockItemData sidN = sidList.get(i);
			StockItemData sidP = sidList.get(i - 1);
			if (sidN.getSma20() >= sidP.getSma20()) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	public double getLatestPriceAboveSma20(List<StockItemData> sidList) {
		StockItemData latest = sidList.get(sidList.size() - 1);
		return StockUtils.roundDoubleDp4((latest.getStockPrice().getClose() - latest.getSma20()) / latest.getSma20());
	}

	public double getLatestK9(List<StockItemData> sidList) {
		StockItemData latest = sidList.get(sidList.size() - 1);
		return latest.getK();
	}

	public double getLatestD9(List<StockItemData> sidList) {
		StockItemData latest = sidList.get(sidList.size() - 1);
		return latest.getD();
	}

	//
	public boolean existsForSma20SelectStrategy2StatsData(String dateString) {
		return sma20SelectStrategy2DAO.existsForStatsData(dateString);
	}

	public void saveSma20SelectStrategy2StatsData(String dateString, LinkedHashMap<String, List<Number>> statsMap)
			throws StockException {
		sma20SelectStrategy2DAO.saveStatsData(dateString, statsMap);
	}

	public LinkedHashMap<String, List<Number>> loadSma20SelectStrategy2StatsData(String dateString)
			throws StockException {
		return sma20SelectStrategy2DAO.loadStatsData(dateString);
	}

	public void preparePriceBreakUpSelectStrategy3() throws StockException {
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			List<StockItemData> sidList = stockItemDataDAO.load(stockSymbol);
			List<StockItemWeeklyData> siwdList = stockItemWeeklyDataDAO.load(stockSymbol);
			TreeMap<Date, Integer> statsMap = calculatePriceBreakUpStatsStrategy3(sidList, siwdList);
			priceBreakUpSelectStrategy3DAO.save(stockSymbol, statsMap);
		}
	}

	public void preparePriceBreakUpSelectStrategy4() throws StockException {
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			List<StockItemData> sidList = stockItemDataDAO.load(stockSymbol);
			TreeMap<Date, List<Number>> statsMap = calculatePriceBreakUpStatsStrategy4(sidList);
			priceBreakUpSelectStrategy4DAO.save(stockSymbol, statsMap);
		}
	}

	public Map<String, Integer> getStatsDataForPriceBreakUpSelectStrategy3(String dateString) {
		if (priceBreakUpSelectStrategy3DAO.resultExistsForDate(dateString)) {
			try {
				return priceBreakUpSelectStrategy3DAO.loadResult(dateString);
			} catch (StockException e) {
				logger.warn("Error loading StatsData for priceBreakUpSelectStrategy3 of date:" + dateString);
				return null;
			}
		} else {
			Map<String, Integer> resultMap = new HashMap<>();
			List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
			for (String stockSymbol : symbolList) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Date, Integer> statsMap = priceBreakUpSelectStrategy3DAO.load(stockSymbol);
					Date current = StockUtils.stringSimpleToDate(dateString).get();
					if (statsMap.containsKey(current)) {
						List<Date> keyList = new ArrayList<>(statsMap.keySet());
						if (keyList.indexOf(current) == 0)
							continue;
						Date previous = keyList.get(keyList.indexOf(current) - 1);
						Integer countCurrent = statsMap.get(current);
						Integer countPrevious = statsMap.get(previous);
						if (countCurrent<countPrevious) continue;
						resultMap.put(stockSymbol, (int) (countCurrent - 0.5*countPrevious));
					} else {
						continue;
					}

				} catch (StockException e) {
					logger.warn("Error loading Stats Data for priceBreakUpSelectStrategy3 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				priceBreakUpSelectStrategy3DAO.saveResult(dateString, resultMap);
			} catch (StockException e) {
				logger.warn("Error saving result data for priceBreakUpSelectStrategy3 of  date:" + dateString);
			}
			return resultMap;
		}
	}

	public Map<String, Integer> getStatsDataForPriceBreakUpSelectStrategy4(String dateString) {
		if (priceBreakUpSelectStrategy4DAO.resultExistsForDate(dateString)) {
			try {
				return priceBreakUpSelectStrategy4DAO.loadResult(dateString);
			} catch (StockException e) {
				logger.warn("Error loading StatsData for priceBreakUpSelectStrategy4 of date:" + dateString);
				return null;
			}
		} else {
			Map<String, Integer> resultMap = new HashMap<>();
			List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
			for (String stockSymbol : symbolList) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Date, List<Number>> statsMap = priceBreakUpSelectStrategy4DAO.load(stockSymbol);
					Date current = StockUtils.stringSimpleToDate(dateString).get();
					if (statsMap.containsKey(current)) {
						List<Date> keyList = new ArrayList<>(statsMap.keySet());
						if (keyList.indexOf(current) == 0)
							continue;
						Date previous = keyList.get(keyList.indexOf(current) - 1);
						List<Number> dataCurrent = statsMap.get(current);
						List<Number> dataPrevious = statsMap.get(previous);
						if (dataCurrent.get(2).doubleValue() < dataPrevious.get(1).doubleValue())
							continue;
						if ((dataPrevious.get(1).doubleValue() == dataPrevious.get(0).doubleValue()))
							continue;
						int score = (int) (100 * (dataCurrent.get(2).doubleValue() - dataPrevious.get(2).doubleValue())
								/ (dataPrevious.get(1).doubleValue() - dataPrevious.get(0).doubleValue()));

						resultMap.put(stockSymbol, score);
					} else {
						continue;
					}

				} catch (StockException e) {
					logger.warn("Error loading Stats Data for priceBreakUpSelectStrategy4 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				priceBreakUpSelectStrategy4DAO.saveResult(dateString, resultMap);
			} catch (StockException e) {
				logger.warn("Error saving result data for priceBreakUpSelectStrategy4 of  date:" + dateString);
			}
			return resultMap;
		}
	}

	/*
	 * The statsMap is a TreeMap with key of trading date and value of an integer
	 * represents the number of times the highest price of the target trading date
	 * breaks above the highest price of each past week.
	 */
	private TreeMap<Date, Integer> calculatePriceBreakUpStatsStrategy3(List<StockItemData> sidList,
			List<StockItemWeeklyData> siwdList) {
		TreeMap<Date, Integer> statsMap = new TreeMap<>();
		for (StockItemData sid : sidList) {
			Double hPrice = sid.getStockPrice().getHigh();
			Date d = sid.getTradingDate();
			int count = 0;
			for (StockItemWeeklyData siwd : siwdList) {
				if (siwd.getTradingDate().before(d)) {
					if (siwd.getStockPrice().getHigh() <= hPrice) {
						count++;
					}
				} else {
					break;
				}
			}
			statsMap.put(d, Integer.valueOf(count));
		}
		return statsMap;
	}

	private TreeMap<Date, List<Number>> calculatePriceBreakUpStatsStrategy4(List<StockItemData> sidList) {
		TreeMap<Date, List<Number>> statsMap = new TreeMap<>();
		for (StockItemData sid : sidList) {
			Double max = sidList.get(0).getStockPrice().getHigh();
			Double min = sidList.get(0).getStockPrice().getLow();
			int index = sidList.indexOf(sid);
			for (int i = 1; i <= index; i++) {
				if (sidList.get(i).getStockPrice().getHigh() > max) {
					max = sidList.get(i).getStockPrice().getHigh();
				}
				if (sidList.get(i).getStockPrice().getLow() < min) {
					min = sidList.get(i).getStockPrice().getLow();
				}
			}
			statsMap.put(sid.getTradingDate(), Arrays.<Number>asList(min, max, sid.getStockPrice().getHigh()));
		}
		return statsMap;
	}

}
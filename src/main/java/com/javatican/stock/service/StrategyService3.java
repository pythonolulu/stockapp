package com.javatican.stock.service;

import java.util.ArrayList;
import java.util.Date;
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
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.PriceBreakUpSelectStrategy3DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockItemWeeklyData;
import com.javatican.stock.util.StockUtils;

@Service("strategyService3")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StrategyService3 {

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
	PriceBreakUpSelectStrategy3DAO priceBreakUpSelectStrategy3DAO;

	public void prepareRawStatsData() throws StockException {
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			TreeMap<Date, Double> rawStatsMap = calculateRawStatsData(stockSymbol);
			if (rawStatsMap == null)
				continue;
			priceBreakUpSelectStrategy3DAO.saveRawStatsData(stockSymbol, rawStatsMap);
		}
	}

	public Map<String, Double> getStatsData(String dateString) {
		if (priceBreakUpSelectStrategy3DAO.statsDataExistsForDate(dateString)) {
			try {
				return priceBreakUpSelectStrategy3DAO.loadStatsData(dateString);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 3  of date:" + dateString);
				return null;
			}
		} else {
			Map<String, Double> statsMap = new HashMap<>();
			List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
			for (String stockSymbol : symbolList) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Date, Double> rawStatsMap = priceBreakUpSelectStrategy3DAO.loadRawStatsData(stockSymbol);
					Date current = StockUtils.stringSimpleToDate(dateString).get();
					if (rawStatsMap.containsKey(current)) {
						List<Date> keyList = new ArrayList<>(rawStatsMap.keySet());
						if (keyList.indexOf(current) == 0)
							continue;
						Date previous = keyList.get(keyList.indexOf(current) - 1);
						Double scoreCurrent = rawStatsMap.get(current);
						Double scorePrevious = rawStatsMap.get(previous);
						if (scoreCurrent <= scorePrevious)
							continue;
						// score is following the below formula, x and y are weight factors.
						// x(A(n)-A(n-1)) + y(A(n)) --> (x+y)A(n)-x(A(n-1)) --> x+y =1 and x = 0.5
						statsMap.put(stockSymbol, scoreCurrent - 0.5 * scorePrevious);
					} else {
						continue;
					}

				} catch (StockException e) {
					logger.warn("Error loading stats data for  Strategy 3 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				priceBreakUpSelectStrategy3DAO.saveStatsData(dateString, statsMap);
			} catch (StockException e) {
				logger.warn("Error saving stats data for Strategy 3 of date:" + dateString);
			}
			return statsMap;
		}
	}

	/*
	 * The statsMap is a TreeMap with key of trading date and value of an integer
	 * represents the number of times the highest price of the target trading date
	 * breaks above the highest price of each past week.
	 */

	private TreeMap<Date, Double> calculateRawStatsData(String stockSymbol) {
		TreeMap<Date, Double> rawStatsMap = null;
		try {
			List<StockItemData> sidList = stockItemDataDAO.load(stockSymbol);
			List<StockItemWeeklyData> siwdList = stockItemWeeklyDataDAO.load(stockSymbol);
			rawStatsMap = new TreeMap<>();
			for (StockItemData sid : sidList) {
				Double hPrice = sid.getStockPrice().getHigh();
				Date d = sid.getTradingDate();
				// score: count the number of weekly highest prices that are below the high
				// price of target date, but weights according to the dates, ie the closer the
				// date(the larger the index), the larger is the weight.
				double score = 0.0;
				for (StockItemWeeklyData siwd : siwdList) {
					if (siwd.getTradingDate().before(d)) {
						if (siwd.getStockPrice().getHigh() <= hPrice) {
							score += ((double) (siwdList.indexOf(siwd) + 1)) / siwdList.size();
						} else if (siwd.getStockPrice().getLow() >= hPrice) {
							score -= ((double) (siwdList.indexOf(siwd) + 1)) / siwdList.size();
						}
					} else {
						break;
					}
				}
				rawStatsMap.put(d, score);
			}
		} catch (StockException e) {
			e.printStackTrace();
			rawStatsMap = null;

		}
		return rawStatsMap;
	}

}
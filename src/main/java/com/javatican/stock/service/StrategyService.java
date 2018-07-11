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
	 * probability. holdPeriod: the period of each warrant item to hold
	 * dataDatePeriod: how many days of data to calculate
	 */
	public void callWarrantSelectStrategy1(int holdPeriod, int dataDatePeriod) throws StockException {
		// get the latest 'dataDatePeriod' trading dates.
		List<Date> dateList = tradingDateDAO.findLatestNTradingDateAsc(dataDatePeriod);
		// get all symbols that have call warrants
		Map<String, StockItem> siMap = callWarrantTradeSummaryDAO.getStockItemsWithCallWarrant();
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
			String dateString = StockUtils.dateToSimpleString(d);
			Map<String, WarrantTrade> wtMap = null;
			if ((wtMap = cachedWTMap.get(dateString)) == null) {
				wtMap = warrantTradeDAO.loadCallWarrantsAsMap(dateString);
			} else {
				cachedWTMap.remove(dateString);
			}
			// get the trading date which is of 'holdPeriod' days after the current date
			String dateStringNext = StockUtils.getNextNTradingDate(dateString, holdPeriod, dateList);
			if (dateStringNext == null)
				break;
			Map<String, WarrantTrade> wtMapNext = warrantTradeDAO.loadCallWarrantsAsMap(dateStringNext);
			// store in the cached map
			cachedWTMap.put(dateStringNext, wtMapNext);
			//
			for (StockItem si : siMap.values()) {
				String symbol = si.getSymbol();
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
				upPercentMap.put(dateString, upPercent);
			}
		}
		// get the top lists with the best win percentages
		Map<String, Double> upPercentCountMap = new TreeMap<>();
		Map<String, Double> upPercentAccMap = new TreeMap<>();
		for (String symbol : statsMap.keySet()) {
			Map<String, Double> upPercentMap = statsMap.get(symbol);
			callWarrantSelectStrategy1DAO.save(symbol, upPercentMap);
			long size = upPercentMap.size();
			long up_size = upPercentMap.values().stream().filter(value -> value > 0.0).count();
			double acc_percent = upPercentMap.values().stream().reduce(0.0, (a, b) -> a + b);
			// give a threshold, say, half of the dataDatePeriod-holdPeriod
			if (size > (dataDatePeriod - holdPeriod) / 2) {
				upPercentCountMap.put(symbol, StockUtils.roundDoubleDp4((double) up_size / size));
				upPercentAccMap.put(symbol, StockUtils.roundDoubleDp4(acc_percent));
			}
		}
		upPercentCountMap.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(30)
				.forEach(entry -> {
					logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName() + "), win percentage:"
							+ entry.getValue() + ", avg percentage:" + upPercentAccMap.get(entry.getKey()));

				});
		logger.info("============================");
		upPercentAccMap.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(30)
		.forEach(entry -> {
			logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName() + "), avg percentage:"
					+ entry.getValue() + ", win percentage:" + upPercentCountMap.get(entry.getKey()));

		});
	}

}
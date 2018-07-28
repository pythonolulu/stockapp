package com.javatican.stock.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.javatican.stock.dao.PriceBreakUpSelectStrategy4DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.util.StockUtils;

@Service("strategyService4")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StrategyService4 {

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
	PriceBreakUpSelectStrategy4DAO priceBreakUpSelectStrategy4DAO;

	public void prepareRawStatsData() throws StockException {
		// get all symbols that have call warrants
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			TreeMap<Date, List<Number>> rawStatsMap = calculateRawStatsData(stockSymbol);
			if (rawStatsMap == null)
				continue;
			priceBreakUpSelectStrategy4DAO.saveRawStatsData(stockSymbol, rawStatsMap);
		}
	}

	public Map<String, Integer> getStatsData(String dateString) {
		if (priceBreakUpSelectStrategy4DAO.statsDataExistsForDate(dateString)) {
			try {
				return priceBreakUpSelectStrategy4DAO.loadStatsData(dateString);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 4 of date:" + dateString);
				return null;
			}
		} else {
			Map<String, Integer> statsMap = new HashMap<>();
			List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
			for (String stockSymbol : symbolList) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Date, List<Number>> rawStatsMap = priceBreakUpSelectStrategy4DAO.loadRawStatsData(stockSymbol);
					Date current = StockUtils.stringSimpleToDate(dateString).get();
					if (rawStatsMap.containsKey(current)) {
						List<Date> keyList = new ArrayList<>(rawStatsMap.keySet());
						if (keyList.indexOf(current) == 0)
							continue;
						Date previous = keyList.get(keyList.indexOf(current) - 1);
						List<Number> dataCurrent = rawStatsMap.get(current);
						List<Number> dataPrevious = rawStatsMap.get(previous);
						if (dataCurrent.get(2).doubleValue() < dataPrevious.get(1).doubleValue())
							continue;
						if ((dataPrevious.get(1).doubleValue() == dataPrevious.get(0).doubleValue()))
							continue;
						int score = (int) (100 * (dataCurrent.get(2).doubleValue() - dataPrevious.get(2).doubleValue())
								/ (dataPrevious.get(1).doubleValue() - dataPrevious.get(0).doubleValue()));

						statsMap.put(stockSymbol, score);
					} else {
						continue;
					}

				} catch (StockException e) {
					logger.warn("Error loading stats data for Strategy 4 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				priceBreakUpSelectStrategy4DAO.saveStatsData(dateString, statsMap);
			} catch (StockException e) {
				logger.warn("Error saving stats data for Strategy 4 of  date:" + dateString);
			}
			return statsMap;
		}
	}

	private TreeMap<Date, List<Number>> calculateRawStatsData(String stockSymbol) {
		TreeMap<Date, List<Number>> rawStatsMap = null;
		try {
			List<StockItemData> sidList = stockItemDataDAO.load(stockSymbol);
			rawStatsMap = new TreeMap<>();
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
				rawStatsMap.put(sid.getTradingDate(), Arrays.<Number>asList(min, max, sid.getStockPrice().getHigh()));
			}
		} catch (StockException e) {
			e.printStackTrace();
			rawStatsMap = null;
		}

		return rawStatsMap;
	}
}
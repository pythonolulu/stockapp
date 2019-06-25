package com.javatican.stock.service;

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
import com.javatican.stock.dao.SmaSelectStrategy2DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.util.StockUtils;

@Service("strategyService2")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StrategyService2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	SmaSelectStrategy2DAO smaSelectStrategy2DAO;
	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;

	public void prepareRawStatsData() throws StockException {
		// get all symbols that have call warrants
		// List<String> symbolList =
		// callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		// ryan note: select all symbols 2019/5/26
		List<String> symbolList = stockItemDAO.getAllSymbols();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			TreeMap<Date, List<Number>> rawStatsMap = calculateRawStatsData(stockSymbol);
			if (rawStatsMap == null)
				continue;
			smaSelectStrategy2DAO.saveRawStatsData(stockSymbol, rawStatsMap);
		}
	}

	public Map<String, List<Number>> getStatsData(String dateString, String type) {
		if (smaSelectStrategy2DAO.statsDataExistsForDate(dateString, type)) {
			try {
				return smaSelectStrategy2DAO.loadStatsData(dateString, type);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 2  of date:" + dateString + " and type: " + type);
				return null;
			}
		} else {
			Map<String, List<Number>> statsMap = new HashMap<>();
			// ryan add 2019/5/26
			List<String> symbolList = type.contains("_all") ? stockItemDAO.getAllSymbols()
					: callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
			Date current = StockUtils.stringSimpleToDate(dateString).get();
			for (String stockSymbol : symbolList) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Date, List<Number>> rawStatsMap = smaSelectStrategy2DAO.loadRawStatsData(stockSymbol);
					if (rawStatsMap.containsKey(current)) {
						List<Number> dataList = rawStatsMap.get(current);
						List<Number> filterList = null;
						int count_above_sma20 = dataList.get(0).intValue();
						int count_above_sma60 = dataList.get(1).intValue();
						int count_sma20_up = dataList.get(2).intValue();
						int count_sma60_up = dataList.get(3).intValue();
						double priceAboveSma20 = dataList.get(4).doubleValue();
						double priceAboveSma60 = dataList.get(5).doubleValue();
						double k9 = dataList.get(6).doubleValue();
						double d9 = dataList.get(7).doubleValue();
						switch (type) {
						// Sma20_above
						case "20A":
						case "20A_all":
							if (count_above_sma20 > 0) {
								filterList = Arrays.asList(count_above_sma20, count_sma20_up, priceAboveSma20, k9, d9);
							}
							break;
						// Sma60_above
						case "60A":
						case "60A_all":
							if (count_above_sma60 > 0) {
								filterList = Arrays.asList(count_above_sma60, count_sma60_up, priceAboveSma60, k9, d9);
							}
							break;
						// Sma20_below
						case "20B":
						case "20B_all":
							if (count_above_sma20 < 0) {
								filterList = Arrays.asList(count_above_sma20, count_sma20_up, priceAboveSma20, k9, d9);
							}
							break;
						// Sma60_below
						case "60B":
						case "60B_all":
							if (count_above_sma60 < 0) {
								filterList = Arrays.asList(count_above_sma60, count_sma60_up, priceAboveSma60, k9, d9);
							}
							break;
						}
						if (filterList != null) {
							statsMap.put(stockSymbol, filterList);
						}
					} else {
						continue;
					}

				} catch (StockException e) {
					logger.warn("Error loading stats data for  Strategy 2 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				smaSelectStrategy2DAO.saveStatsData(dateString, type, statsMap);
			} catch (StockException e) {
				logger.warn("Error saving stats data for Strategy 2 of date:" + dateString + " and type: " + type);
			}
			return statsMap;
		}
	}

	/*
	 * The statsMap is a TreeMap with key of trading date and value of an list of
	 * numbers: count_above_sma20, count_above_sma60, count_sma20_up,
	 * count_sma60_up, priceAboveSma20, priceAboveSma60, k9, d9
	 */

	private TreeMap<Date, List<Number>> calculateRawStatsData(String stockSymbol) {
		TreeMap<Date, List<Number>> rawStatsMap = null;
		try {
			List<StockItemData> sidList = stockItemDataDAO.load(stockSymbol);
			if (sidList.isEmpty() || sidList.size() == 1)
				return null;
			rawStatsMap = new TreeMap<>();
			// count_above_sma20: positive means above sma20, negative means below sma20
			int count_above_sma20 = 0;
			// count_sma20_up: positive means sma20 going up, negative means sma20 going
			// down
			int count_sma20_up = 0;
			int count_above_sma60 = 0;
			int count_sma60_up = 0;
			double k9 = 0;
			double d9 = 0;
			for (int i = 1; i < sidList.size(); i++) {
				StockItemData sidN = sidList.get(i);
				StockItemData sidP = sidList.get(i - 1);
				Date d = sidN.getTradingDate();
				// priceAboveSma20: positive means percentage above sma20, negative means
				// percentage below sma20
				double priceAboveSma20 = 0;
				double priceAboveSma60 = 0;
				if (sidN.getSma20() != null) {
					if (sidN.getStockPrice().getClose() >= sidN.getSma20()) {
						if (count_above_sma20 >= 0) {
							count_above_sma20++;
						} else {
							count_above_sma20 = 1;
						}
					} else {
						if (count_above_sma20 >= 0) {
							count_above_sma20 = -1;
						} else {
							count_above_sma20--;
						}
					}
					priceAboveSma20 = StockUtils
							.roundDoubleDp4((sidN.getStockPrice().getClose() - sidN.getSma20()) / sidN.getSma20());
				}
				//
				if (sidN.getSma60() != null) {
					if (sidN.getStockPrice().getClose() >= sidN.getSma60()) {
						if (count_above_sma60 >= 0) {
							count_above_sma60++;
						} else {
							count_above_sma60 = 1;
						}
					} else {
						if (count_above_sma60 >= 0) {
							count_above_sma60 = -1;
						} else {
							count_above_sma60--;
						}
					}
					priceAboveSma60 = StockUtils
							.roundDoubleDp4((sidN.getStockPrice().getClose() - sidN.getSma60()) / sidN.getSma60());
				}
				//
				if (sidP.getSma20() != null && sidN.getSma20() != null) {
					if (sidN.getSma20() >= sidP.getSma20()) {
						if (count_sma20_up >= 0) {
							count_sma20_up++;
						} else {
							count_sma20_up = 1;
						}
					} else {
						if (count_sma20_up >= 0) {
							count_sma20_up = -1;
						} else {
							count_sma20_up--;
						}
					}
				}
				//
				if (sidP.getSma60() != null && sidN.getSma60() != null) {
					if (sidN.getSma60() >= sidP.getSma60()) {
						if (count_sma60_up >= 0) {
							count_sma60_up++;
						} else {
							count_sma60_up = 1;
						}
					} else {
						if (count_sma60_up >= 0) {
							count_sma60_up = -1;
						} else {
							count_sma60_up--;
						}
					}
				}

				k9 = sidN.getK();
				d9 = sidN.getD();
				List<Number> dataList = Arrays.<Number>asList(count_above_sma20, count_above_sma60, count_sma20_up,
						count_sma60_up, priceAboveSma20, priceAboveSma60, k9, d9);
				//
				rawStatsMap.put(d, dataList);
			}

		} catch (StockException e) {
			e.printStackTrace();
			rawStatsMap = null;

		}
		return rawStatsMap;
	}

}
package com.javatican.stock.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.javatican.stock.dao.FinancialInfoDAO;
import com.javatican.stock.dao.FinancialSelectStrategy5DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockPriceDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.FinancialInfo;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockUtils;

@Service("strategyService5")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StrategyService5 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockPriceDAO stockPriceDAO;
	@Autowired
	FinancialInfoDAO financialInfoDAO;
	@Autowired
	FinancialSelectStrategy5DAO financialSelectStrategy5DAO;

	public void prepareRawStatsData() throws StockException {
		// get all symbols that have call warrants
		List<String> symbolList = stockItemDAO.getAllSymbols();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			TreeMap<Integer, List<Number>> rawStatsMap = calculateRawStatsData(stockSymbol);
			if (rawStatsMap == null)
				continue;
			financialSelectStrategy5DAO.saveRawStatsData(stockSymbol, rawStatsMap);
		}
	}

	public Map<String, Map<String, List<Number>>> getStatsData(int year, int period) {
		if (financialSelectStrategy5DAO.statsDataExistsFor(year, period)) {
			try {
				return financialSelectStrategy5DAO.loadStatsData(year, period);
			} catch (StockException e) {
				logger.warn("Error loading stats data for Strategy 5 of year:" + year + ", period:" + period);
				return null;
			}
		} else {
			Map<String, Map<String, List<Number>>> statsMap = new HashMap<>();
			Map<String, StockItem> siMap = stockItemDAO.findAllAsMap();
			outer: for (String stockSymbol : siMap.keySet()) {
				if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
					continue;
				try {
					TreeMap<Integer, List<Number>> rawStatsMap = financialSelectStrategy5DAO
							.loadRawStatsData(stockSymbol);
					Map<String, List<Number>> dataMap = new HashMap<>();
					List<Number> fcfyList = new ArrayList<>();
					double fcfy_sum = 0.0;
					for (int y = year; y > year - period; y--) {
						if (rawStatsMap.containsKey(y)) {
							try {
								// yield = <free cash flow>/<liabilities and equity>
								double yield = rawStatsMap.get(y).get(0).doubleValue()
										/ rawStatsMap.get(y).get(3).doubleValue();
								fcfyList.add(StockUtils.roundDoubleDp4(yield));
								fcfy_sum += yield;
							} catch (Exception ex) {
								continue outer;
							}
						} else {
							continue outer;
						}
					}
					double average_fcfy = StockUtils.roundDoubleDp4(fcfy_sum / period);
					//free cash flow yield individual
					dataMap.put("fcfy_i", fcfyList);
					//free cash flow yield average
					dataMap.put("fcfy_avg",Arrays.<Number>asList(average_fcfy));
					//每股净值 net asset value per share of target 'year' 
					double nav = rawStatsMap.get(year).get(4).doubleValue(); 
					dataMap.put("nav",Arrays.<Number>asList(StockUtils.roundDoubleDp2(nav)));
					//每股盈馀 eps of target 'year' 
					double eps = rawStatsMap.get(year).get(5).doubleValue(); 
					dataMap.put("eps",Arrays.<Number>asList(StockUtils.roundDoubleDp2(eps)));
					//股价净值比(PBR) price/nav 
					double price = siMap.get(stockSymbol).getPrice();
					dataMap.put("pbr",Arrays.<Number>asList(StockUtils.roundDoubleDp2(price/nav)));
					//本益比(PER) price/eps 
					dataMap.put("per",Arrays.<Number>asList(StockUtils.roundDoubleDp2(price/eps)));
					statsMap.put(stockSymbol, dataMap);
				} catch (StockException e) {
					logger.warn("Error loading stats data for  Strategy 5 of symbol:" + stockSymbol);
					continue;
				}
			}
			try {
				financialSelectStrategy5DAO.saveStatsData(year, period, statsMap);
			} catch (StockException e) {
				logger.warn("Error saving stats data for Strategy 5 of year:" + year + ", period:" + period);
			}
			return statsMap;
		}
	}

	/*
	 * The statsMap is a TreeMap with key of trading date and value of an integer
	 * represents the number of times the highest price of the target trading date
	 * breaks above the highest price of each past week.
	 */

	private TreeMap<Integer, List<Number>> calculateRawStatsData(String stockSymbol) {
		Map<Integer, FinancialInfo> fiMap = financialInfoDAO.findAllSeason4BySymbolAsMap(stockSymbol);
		if (fiMap.isEmpty())
			return null;
		TreeMap<Integer, List<Number>> rawStatsMap = new TreeMap<>();
		for (Map.Entry<Integer, FinancialInfo> entry : fiMap.entrySet()) {
			// 0: free cash flow
			// 1: liabilities total
			// 2: equity shareholders
			// 3: liabilities and equity
			// 4: net asset value per share
			// 5: eps
			rawStatsMap.put(entry.getKey(),
					Arrays.asList(entry.getValue().getFreeCashFlow(), entry.getValue().getLiabilitiesTotal(),
							entry.getValue().getEquityShareholdersParent(), entry.getValue().getLiabilitiesAndEquity(),
							entry.getValue().getNetAssetValuePerShare(), entry.getValue().getEps()));
		}
		return rawStatsMap;
	}

}
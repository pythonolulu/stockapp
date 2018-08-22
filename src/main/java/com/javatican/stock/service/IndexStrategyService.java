package com.javatican.stock.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.IndexStrategyDAO;
import com.javatican.stock.dao.SmaSelectStrategy2DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.util.StockUtils;

@Service("indexStrategyService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class IndexStrategyService {

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
	@Autowired
	IndexStrategyDAO indexStrategyDAO;

	public void prepareSmaStatsData() throws StockException {
		// only stocks with call warrants are selected
		List<String> symbolList = callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
		int totalItemCount = 0;
		TreeMap<Date, List<Number>> statsMap = new TreeMap<>();
		for (String stockSymbol : symbolList) {
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
			try {
				TreeMap<Date, List<Number>> rawStatsMap = smaSelectStrategy2DAO.loadRawStatsData(stockSymbol);
				if (rawStatsMap == null)
					continue;
				totalItemCount++;
				for (Date date : rawStatsMap.keySet()) {
					// rawStatsList contains the following data:
					// count_above_sma20, count_above_sma60, count_sma20_up,
					// count_sma60_up, priceAboveSma20, priceAboveSma60, k9, d9
					List<Number> rawStatsList = rawStatsMap.get(date);
					// dataList contains the stats data for the sma, includes:
					// above_sma20_stock_count, above_sma60_stock_count, sma20_up_stock_count,
					// sma60_up_stock_count
					List<Number> dataList;
					if (statsMap.containsKey(date)) {
						dataList = statsMap.get(date);
					} else {
						dataList = Arrays.asList(0, 0, 0, 0);
						statsMap.put(date, dataList);
					}
					if (rawStatsList.get(0).intValue() > 0) {
						dataList.set(0, dataList.get(0).intValue() + 1);
					}
					if (rawStatsList.get(1).intValue() > 0) {
						dataList.set(1, dataList.get(1).intValue() + 1);
					}
					if (rawStatsList.get(2).intValue() > 0) {
						dataList.set(2, dataList.get(2).intValue() + 1);
					}
					if (rawStatsList.get(3).intValue() > 0) {
						dataList.set(3, dataList.get(3).intValue() + 1);
					}
				}
			} catch (StockException e) {
				logger.warn("Error loading sma raw stats data for symbol:" + stockSymbol);
				continue;
			}
		}
		// write to file
		final int count = totalItemCount;
		statsMap.values().stream().forEach(item -> {
			item.set(0, StockUtils.roundDoubleDp4(((double) item.get(0).intValue()) / count));
			item.set(1, StockUtils.roundDoubleDp4(((double) item.get(1).intValue()) / count));
			item.set(2, StockUtils.roundDoubleDp4(((double) item.get(2).intValue()) / count));
			item.set(3, StockUtils.roundDoubleDp4(((double) item.get(3).intValue()) / count));
		});
		indexStrategyDAO.saveSmaStatsData(statsMap);
	}

}
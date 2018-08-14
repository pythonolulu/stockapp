package com.javatican.stock.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.SmaSelectStrategy2DAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
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
	public boolean existsForStatsData(String dateString) {
		return smaSelectStrategy2DAO.existsForStatsData(dateString);
	}

	public void saveStatsData(String dateString, LinkedHashMap<String, List<Number>> statsMap)
			throws StockException {
		smaSelectStrategy2DAO.saveStatsData(dateString, statsMap);
	}

	public LinkedHashMap<String, List<Number>> loadStatsData(String dateString)
			throws StockException {
		return smaSelectStrategy2DAO.loadStatsData(dateString);
	}


}
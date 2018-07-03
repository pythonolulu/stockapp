package com.javatican.stock.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemLogDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemLog;

/*
 * This helper class is injected as a separated bean into StockItemService , so
 * the Spring transaction manager can create new transactions 
 * (Propagation.REQUIRES_NEW) when calling the methods.
 */
@Component("stockItemHelper")
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StockItemHelper {

	@Autowired
	StockItemLogDAO stockItemLogDAO;

	// Note: below are commented out. Directly updating the passed-in StockItemLog
	// instance will result in two updates to the DB.
	// Therefore, get a fresh StockItemLog object from the DB and update this object
	// directly.
	public void updatePriceDateForItem(String symbol, Date latestDate) {
		StockItemLog sil = stockItemLogDAO.findBySymbol(symbol);
		sil.setPriceDate(latestDate);
		// must call save()
		stockItemLogDAO.save(sil);
	}

	public void updateStatsDateForItem(String symbol, Date latestDate) {
		StockItemLog sil = stockItemLogDAO.findBySymbol(symbol);
		sil.setStatsDate(latestDate);
		// must call save()
		stockItemLogDAO.save(sil);
	}

	public void updateChartDateForItem(String symbol, Date latestDate) {
		StockItemLog sil = stockItemLogDAO.findBySymbol(symbol);
		sil.setChartDate(latestDate);
		// must call save()
		stockItemLogDAO.save(sil);
	}
	//
	// public void updatePriceDateForItem(StockItemLog sil, Date latestDate) {
	// sil.setPriceDate(latestDate);
	// //must call save()
	// stockItemLogDAO.save(sil);
	// }

	// public void updateStatsDateForItem(StockItemLog sil, Date latestDate) {
	// sil.setStatsDate(latestDate);
	// //must call save()
	// stockItemLogDAO.save(sil);
	// }

	// public void updateChartDateForItem(StockItemLog sil, Date latestDate) {
	// sil.setChartDate(latestDate);
	// //must call save()
	// stockItemLogDAO.save(sil);
	// }

}

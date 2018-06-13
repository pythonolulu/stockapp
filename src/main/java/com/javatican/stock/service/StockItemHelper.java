package com.javatican.stock.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.model.StockItem;

/*
 * This helper class is injected as a separated bean into StockItemService , so
 * the Spring transaction manager can create new transactions 
 * (Propagation.REQUIRES_NEW) when calling the methods.
 */
@Component("stockItemHelper")
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StockItemHelper {

	@Autowired
	StockItemDAO stockItemDAO;
	public StockItemHelper() {
		// TODO Auto-generated constructor stub
	}

	public void updatePriceDateForItem(String symbol, Date latestDate) {
		StockItem si = stockItemDAO.findBySymbol(symbol);
		si.setPriceDate(latestDate);
		//must call save()
		stockItemDAO.save(si);
	}

	public void updatePriceDateForItem(StockItem si, Date latestDate) {
		si.setPriceDate(latestDate);
		//must call save()
		stockItemDAO.save(si);
	}

	public void updateStatsDateForItem(StockItem si, Date latestDate) {
		si.setStatsDate(latestDate);
		//must call save()
		stockItemDAO.save(si);
	}
}

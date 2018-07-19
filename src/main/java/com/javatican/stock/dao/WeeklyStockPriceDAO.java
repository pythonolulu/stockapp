package com.javatican.stock.dao;

import org.springframework.stereotype.Repository;

/*
 * StockPrice data is store in json file. 
 */
@Repository("weeklyStockPriceDAO")
public class WeeklyStockPriceDAO extends AbstractStockPriceDAO{
	@Override
	public String getResourceFileDir() {
		return "file:./download/price/weekly/";
	}
}

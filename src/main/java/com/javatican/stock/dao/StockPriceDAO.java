package com.javatican.stock.dao;

import org.springframework.stereotype.Repository;

/*
 * StockPrice data is store in json file. 
 */
@Repository("stockPriceDAO")
public class StockPriceDAO extends AbstractStockPriceDAO{
	@Override
	public String getResourceFileDir() {
		return "file:./download/price/";
	}
}

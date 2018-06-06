package com.javatican.stock;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.StockTradeByTrust;

@Repository("stockTradeByTrustDAO")
public class StockTradeByTrustDAO {
	@Autowired
	StockTradeByTrustRepository stockTradeByTrustRepository;
	 
	public void save(StockTradeByTrust stockTradeByTrust) {
		stockTradeByTrustRepository.save(stockTradeByTrust);
	}
	public void saveAll(List<StockTradeByTrust> list) {
		stockTradeByTrustRepository.saveAll(list);
	}
	public Date getLatestTradingDate() {
		return stockTradeByTrustRepository.getLatestTradingDate();
	}
	public List<String> getDistinctStockSymbol(){
		return stockTradeByTrustRepository.getDistinctStockSymbol();
	}
	public List<StockTradeByTrust> getByTradingDate(Date tradingDate){
		return stockTradeByTrustRepository.findByTradingDate(tradingDate);
	}
	
	/*public List<StockTradeByTrust> getByStockSymbol(String stockSymbol){
		return stockTradeByTrustRepository.findByStockSymbol(stockSymbol);
	}*/
	
}

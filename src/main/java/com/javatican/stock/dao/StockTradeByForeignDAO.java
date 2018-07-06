package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.repository.StockTradeByForeignRepository;

@Repository("stockTradeByForeignDAO")
public class StockTradeByForeignDAO {
	@Autowired
	StockTradeByForeignRepository stockTradeByForeignRepository;

	public StockTradeByForeignDAO() {
	}

	public void save(StockTradeByForeign stockTradeByForeign) {
		stockTradeByForeignRepository.save(stockTradeByForeign);
	}

	public void saveAll(List<StockTradeByForeign> list) {
		stockTradeByForeignRepository.saveAll(list);
	}

	public Date getLatestTradingDate() {
		return stockTradeByForeignRepository.getLatestTradingDate();
	}

	public List<String> getDistinctStockSymbol() {
		return stockTradeByForeignRepository.getDistinctStockSymbol();
	}

	public List<StockTradeByForeign> getByTradingDate(Date tradingDate) {
		return stockTradeByForeignRepository.findByTradingDate(tradingDate);
	}

	public List<StockTradeByForeign> getByStockSymbol(String stockSymbol) {
		return stockTradeByForeignRepository.findByStockSymbolOrderByTradingDateAsc(stockSymbol);
	}
}

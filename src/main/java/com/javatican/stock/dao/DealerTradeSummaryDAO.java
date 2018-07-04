package com.javatican.stock.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.DealerTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.repository.CallWarrantTradeSummaryRepository;
import com.javatican.stock.repository.DealerTradeSummaryRepository;

@Repository("dealerTradeSummaryDAO")
public class DealerTradeSummaryDAO {
	@Autowired
	DealerTradeSummaryRepository dealerTradeSummaryRepository;

	public DealerTradeSummaryDAO() {
	}

	public void save(DealerTradeSummary dealerTradeSummary) {
		dealerTradeSummaryRepository.save(dealerTradeSummary);
	}

	public void saveAll(Collection<DealerTradeSummary> list) {
		dealerTradeSummaryRepository.saveAll(list);
	}

	public List<DealerTradeSummary> getByTradingDate(Date tradingDate) {
		return dealerTradeSummaryRepository.findByTradingDate(tradingDate);
	}

	public List<DealerTradeSummary> getByStockSymbol(String stockSymbol) {
		return dealerTradeSummaryRepository.findByStockSymbolOrderByTradingDateAsc(stockSymbol);
	}

	public Date getLatestTradingDate() {
		return dealerTradeSummaryRepository.getLatestTradingDate();
	}


}

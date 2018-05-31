package com.javatican.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

@Service("stockTradeByTrustService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockTradeByTrustService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_STOCK_TRADE_BY_TRUST_GET_URL = "http://www.tse.com.tw/fund/TWT44U?response=html&date=%s";

	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;

	@Autowired
	TradingDateDAO tradingDateDAO;

	/*
	 * update stock trade data for Trust (dynamic download and save the new available data)
	 */
	public void updateData() throws StockException{
		 Date latest = stockTradeByTrustDAO.getLatestTradingDate();
		 List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		 downloadAndSave(tdList);
	}
	/*
	 * manual downloads history stock trade data by Trust (only run once)
	 */
	public void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(
				StockUtils.stringToDate("2018/05/01").get(), 
				StockUtils.stringToDate("2018/05/27").get());
		downloadAndSave(tdList);
		
	}
	/*
	 * download and save the stock trade data by Trust for a list of TradingDate instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException{
		List<StockTradeByTrust> result = new ArrayList<>();
		try {
			for (TradingDate td : tdList) {
				String dateString = StockUtils.dateToSimpleString(td.getDate());
				logger.info("prepare data for date:"+dateString);
				Document doc = Jsoup.connect(String.format(TWSE_STOCK_TRADE_BY_TRUST_GET_URL, dateString)).get();
				Elements trs = doc.select("table > tbody > tr");
				StockTradeByTrust stbt;
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					stbt = new StockTradeByTrust(td.getDate());
					stbt.setStockSymbol(tds.get(1).text());
					stbt.setBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					stbt.setSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					stbt.setDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					result.add(stbt);
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
				}

			}
			stockTradeByTrustDAO.saveAll(result);
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}
}

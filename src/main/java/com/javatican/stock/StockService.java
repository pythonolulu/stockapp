package com.javatican.stock;

import java.io.IOException;
import java.util.Arrays;
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

import com.javatican.stock.model.TradingDate;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.util.StockUtils;

@Service("stockService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_DAILY_TRADING_GET_URL = "http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=%s";
	private static final String TWSE_TRADING_VALUE_FOREIGN_URL_GET = "http://www.tse.com.tw/en/fund/BFI82U?response=html&dayDate=%s&type=day";
	
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	TradingValueDAO tradingValueDAO;

	/*
	 * method to update trading dates and total trading values
	 */
	public void updateTradingDateAndValue() throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_DAILY_TRADING_GET_URL, StockUtils.todayDateString())).get();
			// StockUtils.writeDocumentToFile(doc, "test.html");
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Element td = tr.selectFirst("td");
				Date date = StockUtils.stringToDate(td.text()).get();
				if (tradingDateDAO.existsByDate(date)) {
					logger.info("skipping updating trading date and values for date:" + date);
					continue;
				} else {
					TradingDate tDate = new TradingDate();
					tDate.setDate(date);
					tradingDateDAO.saveTradingDate(tDate);
					//
					Elements tds = tr.select("td");
					TradingValue tValue = new TradingValue();
					tValue.setTradingDate(date);
					tValue.setTotalValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					// set trading values for foreign and other investors
					setForeignAndOtherInvestorsTradingValue(StockUtils.dateToSimpleString(tDate.getDate()), tValue);
					tradingValueDAO.saveTradingValue(tValue);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
					}
				}
			}
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

	private void initialTradingDateAndValue(String dateString) throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_DAILY_TRADING_GET_URL, dateString)).get();
			// StockUtils.writeDocumentToFile(doc, "TWSE_DAILY_TRADING.html");
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				TradingDate tDate = new TradingDate();
				tDate.setDateAsString(tds.get(0).text());
				tradingDateDAO.saveTradingDate(tDate);
				//
				TradingValue tValue = new TradingValue();
				tValue.setTradingDate(tDate.getDate());
				tValue.setTotalValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				// set trading values for foreign and other investors
				setForeignAndOtherInvestorsTradingValue(StockUtils.dateToSimpleString(tDate.getDate()), tValue);
				tradingValueDAO.saveTradingValue(tValue);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
				}
			}
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

	/*
	 * method to download and store in DB the trading dates and total trading values
	 */
	public void prepareData() throws StockException {
		List<String> dateList = Arrays.asList("20180201", "20180301", "20180401", "20180501");
		for (String dateString : dateList) {
			initialTradingDateAndValue(dateString);
		}
	}

	private void setForeignAndOtherInvestorsTradingValue(String dateString, TradingValue tradingValue)
			throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_TRADING_VALUE_FOREIGN_URL_GET, dateString)).get();
			//StockUtils.writeDocumentToFile(doc, "TWSE_TRADING_VALUE_FOREIGN.html");
			Elements trs = doc.select("table > tbody > tr");
			Elements tds;
			// 1st tr Dealers (Proprietary)
			tds = trs.get(0).select("td");
			// 2nd td buy
			tradingValue.setDealerBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
			// 3rd td sell
			tradingValue.setDealerSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			// 4th td diff
			tradingValue.setDealerDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			// 2nd tr Dealers (Hedge)
			tds = trs.get(1).select("td");
			// 2nd td buy
			tradingValue.setDealerHedgeBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
			// 3rd td sell
			tradingValue.setDealerHedgeSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			// 4th td diff
			tradingValue.setDealerHedgeDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			// 3rd tr Securities Investment Trust Companies(trust)
			tds = trs.get(2).select("td");
			// 2nd td buy
			tradingValue.setTrustBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
			// 3rd td sell
			tradingValue.setTrustSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			// 4th td diff
			tradingValue.setTrustDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			// 4th tr Foreign Investors include Mainland Area Investors(Foreign Dealers
			// excluded)
			tds = trs.get(3).select("td");
			// 2nd td buy
			tradingValue.setForeignBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
			// 3rd td sell
			tradingValue.setForeignSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			// 4th td diff
			tradingValue.setForeignDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

}

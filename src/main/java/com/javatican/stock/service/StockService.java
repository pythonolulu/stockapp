package com.javatican.stock.service;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

import com.javatican.stock.StockConfig;
import com.javatican.stock.StockException;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.util.StockUtils;

/* this service deals with downloading and saving trading date and total trading values
 * and trading values for three big investors
 */
@Service("stockService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_DAILY_TRADING_GET_URL = "http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=%s";
	private static final String TWSE_TRADING_VALUE_FOREIGN_GET_URL = "http://www.tse.com.tw/en/fund/BFI82U?response=html&dayDate=%s&type=day";

	@Autowired
	StockConfig stockConfig;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	TradingValueDAO tradingValueDAO;
	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockItemDataDAO stockItemDataDAO;

	/*
	 * update trading dates and total trading values and trading values for 3 big
	 * investors. The download contains the data for the current month, so
	 * duplicates are checked before save into db
	 * 
	 * This shall be run after every trading date.
	 */
	public void updateTradingDateAndValue() throws StockException {
		String dateString = StockUtils.todayDateString();
		downloadAndSaveTradingDateAndValueForTheMonth(dateString, true);
	}

	/*
	 * download and store the trading dates and total trading values and trading
	 * values for 3 big investors for the past 6 month period
	 * 
	 * need to run only once
	 */
	private void prepareData() throws StockException {
		List<String> dateList = StockUtils.calculateDateStringPastSixMonth();
		for (String dateString : dateList) {
			downloadAndSaveTradingDateAndValueForTheMonth(dateString, false);
		}
	}

	/*
	 * check Date data: this may happen when crossing a new month, so we check the
	 * missing TradingDate item and do the download and save.
	 */
	public void checkData() throws StockException {
		List<String> dateList = StockUtils.calculateDateStringPastSixMonth();
		for (String dateString : dateList) {
			downloadAndSaveTradingDateAndValueForTheMonth(dateString, true);
		}
	}

	/*
	 * download and store the trading date and total trading values for a specific
	 * month. It calls setForeignAndOtherInvestorsTradingValue()
	 */
	private void downloadAndSaveTradingDateAndValueForTheMonth(String dateString, boolean checkDuplicate)
			throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_DAILY_TRADING_GET_URL, dateString)).get();
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Element td = tr.selectFirst("td");
				Date date = StockUtils.stringToDate(td.text()).get();
				// check duplicates
				if (checkDuplicate && tradingDateDAO.existsByDate(date)) {
					logger.info("Data exist. Skipping updating trading date and values for date:" + date);
					continue;
				} else {
					TradingDate tDate = new TradingDate();
					tDate.setDate(date);
					tradingDateDAO.save(tDate);

					Elements tds = tr.select("td");
					//
					TradingValue tValue = new TradingValue();
					tValue.setTradingDate(date);
					tValue.setTotalValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					// set trading values for foreign and other investors
					setForeignAndOtherInvestorsTradingValue(StockUtils.dateToSimpleString(tDate.getDate()), tValue);
					tradingValueDAO.save(tValue);
					try {
						Thread.sleep(stockConfig.getSleepTime());
					} catch (InterruptedException ex) {
					}
				}
			}
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	/*
	 * download and parse Trading values for Foreign and other investors for a
	 * specific trading date
	 */
	private void setForeignAndOtherInvestorsTradingValue(String dateString, TradingValue tradingValue)
			throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_TRADING_VALUE_FOREIGN_GET_URL, dateString)).get();
			// StockUtils.writeDocumentToFile(doc, "TWSE_TRADING_VALUE_FOREIGN.html");
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
	/*
	 * public List<StockTradeByTrust> getTop30StockTradeByTrust(Date tradingDate) {
	 * List<StockTradeByTrust> stbtList =
	 * stockTradeByTrustDAO.getByTradingDate(tradingDate); stbtList =
	 * stbtList.stream().sorted((stbt1, stbt2) -> { double price1 =
	 * stbt1.getStockItem().getPrice(); double price2 =
	 * stbt2.getStockItem().getPrice(); double amt1 = price1 * (stbt1.getBuy() +
	 * stbt1.getSell()); double amt2 = price2 * (stbt2.getBuy() + stbt2.getSell());
	 * return -1 * Double.compare(amt1, amt2);
	 * }).limit(30).collect(Collectors.toList()); return stbtList; }
	 */

	public List<Date> getLatestNTradingDate(int dateLength) {
		return tradingDateDAO.findLatestNTradingDate(dateLength);
	}

	public Map<StockItem, Map<String, StockTradeByTrust>> getTop30StockItemTradeByTrust(Date tradingDate,
			int dateLength) {
		// 1. get the stbt list for the specified date
		List<StockTradeByTrust> stbtList = stockTradeByTrustDAO.getByTradingDate(tradingDate);
		// 2. sort the stbt list by (buy+sell)*price amount in descending order
		// and select the top 30 items and collect its stockItem relationship objects
		// (note that the stockItem object has its 'stbt' relationship retrieved from
		// DB.)
		List<StockItem> siList = stbtList.stream().sorted((stbt1, stbt2) -> {
			double price1 = stbt1.getStockItem().getPrice();
			double price2 = stbt2.getStockItem().getPrice();
			double amt1 = price1 * (stbt1.getBuy() + stbt1.getSell());
			double amt2 = price2 * (stbt2.getBuy() + stbt2.getSell());
			return -1 * Double.compare(amt1, amt2);
		}).limit(30).map(stbt -> stbt.getStockItem()).collect(Collectors.toList());
		// 3. query the latest 10 trading dates
		List<Date> dList = tradingDateDAO.findLatestNTradingDate(dateLength);
		// 4. map object to return to caller
		// key is the stockItem
		// value is a sub-map object with key of trading date and value of
		// StockTradeByTrust object
		Map<StockItem, Map<String, StockTradeByTrust>> dataMap = new LinkedHashMap<>();
		// 5. filter the 'stbt' collection using latest 10 trading dates list.
		// and create the sub-map object
		siList.stream().forEach(si -> {
			Map<String, StockTradeByTrust> map = new TreeMap<>();
			si.getStbt().stream().filter(stbt -> dList.contains(stbt.getTradingDate()))
					.forEach(stbt -> map.put(StockUtils.dateToStringSeparatedBySlash(stbt.getTradingDate()), stbt));
			// map.values().stream().forEach(stbt->logger.info(stbt.toString()));
			dataMap.put(si, map);
		});
		return dataMap;
	}

	public Map<StockItem, Map<String, StockItemData>> getStockItemStatsData(List<StockItem> siList, int dateLength) {
		List<Date> dList = tradingDateDAO.findLatestNTradingDate(dateLength);
		Map<StockItem, Map<String, StockItemData>> statsMap = new LinkedHashMap<>();
		siList.stream().forEach(si -> {
			try {
				List<StockItemData> sidList = stockItemDataDAO.load(si.getSymbol());
				Map<String, StockItemData> map = new TreeMap<>();
				sidList.stream().filter(sid -> dList.contains(sid.getTradingDate()))
						.forEach(sid -> map.put(StockUtils.dateToStringSeparatedBySlash(sid.getTradingDate()), sid));
				statsMap.put(si, map);
			} catch (StockException ex) {
				logger.warn("Can not load the stats data for stock symbol: " + si.getSymbol());
			}

		});
		return statsMap;
	}

}

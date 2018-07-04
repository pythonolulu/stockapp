package com.javatican.stock.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.WarrantTradeDAO;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.model.WarrantTrade;
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.util.StockUtils;

@Service("callWarrantTradeSummaryService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class CallWarrantTradeSummaryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_CALL_WARRANT_TRADE_GET_URL = "http://www.tse.com.tw/exchangeReport/MI_INDEX?response=html&date=%s&type=0999";

	@Autowired
	StockConfig stockConfig;

	@Autowired
	TradingDateDAO tradingDateDAO;

	@Autowired
	StockItemDAO stockItemDAO;

	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;

	@Autowired
	WarrantTradeDAO warrantTradeDAO;

	@Autowired
	StockItemService stockItemService;

	/*
	 * update warrant trade data (download and save the new available data)
	 */
	public void updateData() throws StockException {
		Date latest = callWarrantTradeSummaryDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		downloadAndSave(tdList);
	}

	/*
	 * manual downloads history warrant trade data (only run once)
	 */
	public void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(StockUtils.stringToDate("2018/03/30").get(),
				StockUtils.stringToDate("2018/03/30").get());
		//downloadAndSave(tdList);
		downloadAndSaveWT(tdList);

	}

	/*
	 * download and save the warrant trade data for a list of TradingDate instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException {
		Map<String, StockItem> siMap = stockItemDAO.findAllAsMap();
		Map<String, CallWarrantTradeSummary> cwtsMap;
		List<WarrantTrade> wtList;
		for (TradingDate td : tdList) {
			cwtsMap = new TreeMap<>();
			wtList = new ArrayList<>();
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			logger.info("prepare call warrant data for date:" + dateString);

			String strUrl = String.format(TWSE_CALL_WARRANT_TRADE_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);

				Elements trs = doc.select("table > tbody > tr");
				CallWarrantTradeSummary cwts;
				StockItem si = null;
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					// get the stock symbol
					String symbol = tds.get(17).text();
					// bugger! some symbol field is empty
					if (symbol.equals(""))
						continue;
					// check if WarrantTradeSummary instance exists?
					if ((cwts = cwtsMap.get(symbol)) == null) {
						cwts = new CallWarrantTradeSummary(td.getDate(), symbol);
						//
						si = siMap.get(symbol);
						if (si == null) {
							si = stockItemService.downloadAndSaveStockProfile(symbol);
							try {
								Thread.sleep(stockConfig.getSleepTime());
							} catch (InterruptedException ex) {
							}
							siMap.put(symbol, si);
						}
						cwts.setStockItem(si);
						cwtsMap.put(symbol, cwts);
					}
					// accumulate various values
					cwts.setTransaction(
							cwts.getTransaction() + Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					//
					Double tradeValue = Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text()));
					cwts.setTradeValue(cwts.getTradeValue() + tradeValue);
					// add into WarrantTrade list
					Double volume = Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text()));
					if (volume != 0.0) {
						WarrantTrade wt = new WarrantTrade();
						wt.setWarrantSymbol(tds.get(1).text());
						wt.setAvgPrice(StockUtils.roundDoubleDp2(tradeValue / volume));
						wt.setStockSymbol(symbol);
						wtList.add(wt);
					}
				}
				cwtsMap.values().stream().forEach(item -> {
					if (item.getTransaction() == 0) {
						item.setAvgTransactionValue(0.0);
					} else {
						item.setAvgTransactionValue(
								StockUtils.roundDoubleDp0(item.getTradeValue() / item.getTransaction()));
					}
				});
				callWarrantTradeSummaryDAO.saveAll(cwtsMap.values());
				warrantTradeDAO.saveCallWarrants(dateString, wtList);
			} catch (Exception ex) {
				throw new StockException(ex);
			}
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
		}

	}
	/*
	 * below only save warrant trade data
	 * 
	 */

	private void downloadAndSaveWT(List<TradingDate> tdList) throws StockException {
		List<WarrantTrade> wtList;
		for (TradingDate td : tdList) {
			wtList = new ArrayList<>();
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			logger.info("prepare call warrant data for date:" + dateString);

			String strUrl = String.format(TWSE_CALL_WARRANT_TRADE_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);

				Elements trs = doc.select("table > tbody > tr");
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					// get the stock symbol
					String symbol = tds.get(17).text();
					// bugger! some symbol field is empty
					if (symbol.equals(""))
						continue;

					Double tradeValue = Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text()));

					// add into WarrantTrade list
					Double volume = Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text()));
					if (volume != 0.0) {
						WarrantTrade wt = new WarrantTrade();
						wt.setWarrantSymbol(tds.get(1).text());
						wt.setAvgPrice(StockUtils.roundDoubleDp2(tradeValue / volume));
						wt.setStockSymbol(symbol);
						wtList.add(wt);
					}
				}
				warrantTradeDAO.saveCallWarrants(dateString, wtList);
			} catch (Exception ex) {
				throw new StockException(ex);
			}
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
		}

	}
}

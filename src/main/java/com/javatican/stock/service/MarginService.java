package com.javatican.stock.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.javatuples.Pair;
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
import com.javatican.stock.dao.MarginSblWithDateDAO;
import com.javatican.stock.dao.MarginSblWithSymbolDAO;
import com.javatican.stock.dao.MarginTradeDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.MarginSblWithDate;
import com.javatican.stock.model.MarginSblWithSymbol;
import com.javatican.stock.model.MarginTrade;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

@Service("marginService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class MarginService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String MARGIN_TRADE_GET_URL = "https://www.twse.com.tw/exchangeReport/MI_MARGN?response=html&date=%s&selectType=ALL";
	private static final String SBL_TRADE_GET_URL = "https://www.twse.com.tw/exchangeReport/TWT93U?response=html&date=%s";

	@Autowired
	StockConfig stockConfig;
	@Autowired
	MarginSblWithSymbolDAO marginSblWithSymbolDAO;
	@Autowired
	MarginSblWithDateDAO marginSblWithDateDAO;
	@Autowired
	MarginTradeDAO marginTradeDAO;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	StockItemDAO stockItemDAO;

	/*
	 * update margin and sbl trade data (download and save the new available data)
	 */
	public void updateData() throws StockException {
		Date latest = marginSblWithSymbolDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		downloadAndSave(tdList);
	}

	/*
	 * manual downloads history margin and sbl trade data (only run once)
	 */
	private void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(StockUtils.stringToDate("2018/04/01").get(),
				StockUtils.stringToDate("2018/07/06").get());
		downloadAndSave(tdList);

	}

	public void extractMarginData() throws StockException {
		List<Date> dateList = marginSblWithSymbolDAO.getDataDateList();
		List<String> symbolList = stockItemDAO.getAllSymbols();
		Map<String, List<MarginSblWithDate>> mswdMap = new TreeMap<>();
		symbolList.stream().forEach(symbol -> {
			List<MarginSblWithDate> mswdList = new ArrayList<>();
			mswdMap.put(symbol, mswdList);
		});
		for (Date date : dateList) {
			List<MarginSblWithSymbol> mswsList = marginSblWithSymbolDAO.load(date);
			for (MarginSblWithSymbol msws : mswsList) {
				String symbol = msws.getSymbol();
				List<MarginSblWithDate> mswdList = mswdMap.get(symbol);
				if (mswdList == null)
					continue;
				MarginSblWithDate mswd = new MarginSblWithDate(date, msws);
				mswdList.add(mswd);
			}
		}
		// save data
		mswdMap.keySet().stream().forEach(symbol -> {
			List<MarginSblWithDate> mswdList = mswdMap.get(symbol);
			try {
				marginSblWithDateDAO.save(symbol, mswdList);
			} catch (StockException e) {
				logger.warn("Error saving Margin and SBL data for symbol:" + symbol);
			}
		});
	}

	/*
	 * download and save data for a list of TradingDate instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException {
		Map<String, MarginSblWithSymbol> mswsMap;
		for (TradingDate td : tdList) {
			mswsMap = new TreeMap<>();
			//
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			logger.info("prepare margin data for date:" + dateString);

			String strUrl = String.format(MARGIN_TRADE_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
				Elements tables = doc.select("body > div > table");
				if (tables == null || tables.isEmpty()) {
					continue;
				}
				// read overall margin trade data(1st table)
				Elements trs = tables.get(0).select("tbody > tr");
				if (trs == null || trs.isEmpty()) {
					continue;
				}
				MarginTrade mt = new MarginTrade(td.getDate());
				// 1st row
				Elements tds = trs.get(0).select("td");
				mt.setMarginBuy(Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(1).text()))
						- Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(3).text())));
				mt.setMarginRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				mt.setMarginAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				// 2nd row
				tds = trs.get(1).select("td");
				mt.setShortSell(Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(2).text()))
						- Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(3).text())));
				mt.setShortRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
				mt.setShortAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				// 3rd row
				tds = trs.get(2).select("td");
				mt.setMarginBuyValue(Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(1).text()))
						- Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(3).text())));
				mt.setMarginRedempValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				mt.setMarginAccValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				// save
				marginTradeDAO.save(mt);
				// read margin data for each stock(2nd table)
				trs = tables.get(1).select("tbody > tr");
				for (Element tr : trs) {
					tds = tr.select("td");
					// get the stock symbol
					String symbol = tds.get(0).text();
					// bugger! some symbol field is empty
					if (symbol.equals(""))
						continue;
					MarginSblWithSymbol msws = new MarginSblWithSymbol(symbol);
					//
					msws.setBuy(Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(2).text()))
							- Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(4).text())));
					msws.setBuyRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					msws.setBuyAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
					msws.setLimit(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text())));

					msws.setShortRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
					msws.setShortSell(Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(9).text()))
							- Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(10).text())));
					msws.setShortAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(12).text())));
					//
					mswsMap.put(symbol, msws);
				}
			} catch (Exception ex) {
				throw new StockException(ex);
			}
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
			// download sbl data
			strUrl = String.format(SBL_TRADE_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
				Elements trs = doc.select("table > tbody > tr");
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					String symbol = tds.get(0).text();
					// bugger! some symbol field is empty
					if (symbol.equals(""))
						continue;
					MarginSblWithSymbol msws = null;
					// check if MarginSblWithSymbol instance exists?
					if ((msws = mswsMap.get(symbol)) == null) {
						msws = new MarginSblWithSymbol(symbol);
						mswsMap.put(symbol, msws);
					}
					//
					msws.setSblRedemp(StockUtils.roundDoubleDp0(
							Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(10).text())) / 1000));
					msws.setSbl(StockUtils
							.roundDoubleDp0((Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(9).text()))
									+ Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(11).text()))) / 1000));
					msws.setSblAcc(StockUtils.roundDoubleDp0(
							Double.parseDouble(StockUtils.removeCommaInNumber(tds.get(12).text())) / 1000));

				}

				marginSblWithSymbolDAO.save(dateString, mswsMap.values());
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
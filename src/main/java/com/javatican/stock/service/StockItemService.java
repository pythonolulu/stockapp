package com.javatican.stock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
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
import com.javatican.stock.dao.StockPriceDAO;
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockPrice;
import com.javatican.stock.util.StockUtils;

/*
 * This service instance is used to perform actions on individual stocks, such as
 * download and save stock profile, stock price.
 */
@Service("stockItemService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockItemService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// Url for downloading stock daily trading value/volume and prices(ohlc)
	private static final String TWSE_INDIVIDUAL_STOCK_DAILY_TRADING_GET_URL = "http://www.tse.com.tw/en/exchangeReport/STOCK_DAY?response=html&date=%s&stockNo=%s";

	// urls for downloading stock profile(name, capital)
	private static final String TWSE_STOCK_PROFILE_GET_URL = "http://mops.twse.com.tw/mops/web/t05st03";
	private static final String TWSE_STOCK_PROFILE_POST_URL = "http://mops.twse.com.tw/mops/web/ajax_t05st03";
	@Autowired
	StockConfig stockConfig;
	@Autowired
	StockPriceDAO stockPriceDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	@Autowired
	TradingDateDAO tradingDateDAO;

	/*
	 * batch job to download and save prices for any new stock items
	 */
	public void downloadAndSaveStockPrices() throws StockException {
		Collection<String> stockSymbols = stockItemDAO.getAllSymbols();
		List<String> toSaveList = stockSymbols.stream().filter(symbol -> !stockPriceDAO.existsForSymbol(symbol))
				.collect(Collectors.toList());
		preparePriceDataForSymbols(toSaveList);
	}

	/*
	 * create a StockItem
	 */
	public StockItem downloadAndSaveStockProfile(String symbol) throws StockException {
		return downloadAndSaveStockProfiles(Arrays.asList(symbol)).iterator().next();
	}

	/*
	 * create new stock items passing a list of new symbols the price field is not
	 * yet updated.
	 */
	private Iterable<StockItem> downloadAndSaveStockProfiles(Collection<String> symbols) throws StockException {
		Pattern cnameP = Pattern.compile("\\((\\S+)\\)\\s(\\S+)");
		Pattern capitalP = Pattern.compile("\\s*([\\d,-]+)元");
		try {
			// connect to first url to set the session id
			Connection.Response res = Jsoup.connect(TWSE_STOCK_PROFILE_GET_URL).method(Method.GET).execute();
			String sessionId = res.cookie("jcsession");
			// System.out.println(sessionId);
			Map<String, String> reqParams = new HashMap<>();
			reqParams.put("encodeURIComponent", "1");
			reqParams.put("step", "1");
			reqParams.put("firstin", "1"); // important param
			reqParams.put("off", "1");
			reqParams.put("keyword4", "");
			reqParams.put("code1", "");
			reqParams.put("TYPEK2", "");
			reqParams.put("checkbtn", "");
			reqParams.put("queryName", "co_id");
			reqParams.put("inpuType", "co_id");
			reqParams.put("TYPEK", "all");
			List<StockItem> siList = new ArrayList<>();
			StockItem si = null;
			for (String symbol : symbols) {
				logger.info("downloading stock profile for symbol:" + symbol);
				reqParams.put("co_id", symbol);
				// connect to 2nd url and pass in the session id
				Document doc = Jsoup.connect(TWSE_STOCK_PROFILE_POST_URL).cookie("jcsession", sessionId).data(reqParams)
						.timeout(0).post();
				si = new StockItem();
				si.setSymbol(symbol);
				Element compNameElement = doc.selectFirst("table td.compName span");
				// the url may return nothing for some symbols, such as 0050.
				if (compNameElement != null) {
					Matcher m = cnameP.matcher(compNameElement.text());
					if (m.find()) {
						// System.out.println("First: " + m.group(1));
						// System.out.println("Last: " + m.group(2));
						si.setName(m.group(2));
						si.setCategory(m.group(1));
					} else {
						throw new StockException("Error when parsing the company name");
					}
					// 實收資本額
					String capitalText = doc.select("th:contains(實收資本額) + td").text();
					m = capitalP.matcher(capitalText);
					if (m.find()) {
						// System.out.println("First: " + m.group(1));
						si.setCapital(Double.parseDouble(StockUtils.removeCommaInNumber(m.group(1))));
					} else {
						throw new StockException("Error when parsing the capital");
					}
				} else {
					si.setName(symbol);
					si.setCategory("其它");
					si.setCapital(0.0);
				}
				siList.add(si);
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			}
			return stockItemDAO.saveAll(siList);
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

	/*
	 * update the price field in stockItem for all items. Note: just call its setter
	 * method , no need to call save()
	 */
	public void updateStockItemPriceFieldForAllSymbols() {
		List<StockItem> siList = stockItemDAO.findAll();
		updatePriceFieldOfStockItems(siList);
	}

	/*
	 * Calculate the average closing price for the last month for the stock item.
	 * Note: just call its setter method , no need to call save().
	 */
	public void updateStockItemPriceField(String stockSymbol) {
		StockItem si = stockItemDAO.findBySymbol(stockSymbol);
		updatePriceFieldOfStockItems(Arrays.asList(si));
	}

	/*
	 * calculate the average closing price for the last month(price field in
	 * StockItem) for any stock items which has the empty(equals to 0.0) price
	 * field.
	 */
	public void updateMissingStockItemPriceField() {
		List<StockItem> missingFieldItems = stockItemDAO.findByPrice(0.0);
		updatePriceFieldOfStockItems(missingFieldItems);
	}
	
	/*
	 * Calculate the average closing price for the last month for the list of stock
	 * items. Note: just call its setter method , no need to call save().
	 */
	private void updatePriceFieldOfStockItems(List<StockItem> siList) {
		Pair<Date, Date> tuple = StockUtils.getFirstAndLastDayOfLastMonth();
		Date start = tuple.getValue0();
		Date end = tuple.getValue1();
		for (StockItem si : siList) {
			try {
				List<StockPrice> spList = stockPriceDAO.loadBetweenDate(si.getSymbol(), start, end);
				double average = spList.stream().mapToDouble(StockPrice::getClose).average().getAsDouble();
				si.setPrice(average);
			} catch (Exception ex) {
				logger.warn("Cannot load price data. The price field for the stock item " + si.getSymbol()
						+ " can not be calculated.");
			}
		}
	}

	/*
	 * Update stock daily trading value/volume and prices for a stock. The data
	 * downloaded include records for the whole month, some may have been downloaded
	 * before. The duplicate ones will be checked in
	 * StockPriceDAO.addStockPriceList() method.
	 * 
	 * this method shall be run when newest price data is required on-demand
	 */
	public void updatePriceDataForSymbol(String stockSymbol) throws StockException {
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		List<StockPrice> spList = downloadStockPriceAndVolume(stockSymbol, firstDayOfTheMonth);
		stockPriceDAO.update(stockSymbol, spList);
	}

	/*
	 * get the latest stock price data for the specified stock item.
	 * It will try to download any new price data if available.
	 */
	public List<StockPrice> getLatestStockPrices(String symbol) throws StockException{
		Date latestTradingDate = tradingDateDAO.getLatestTradingDate();
		List<StockPrice> existingData = stockPriceDAO.load(symbol);
		Date latestDateForPriceData = stockPriceDAO.getLatestDateForPriceData(existingData);
		if(latestTradingDate.after(latestDateForPriceData)) {
			//new data available, so update prices
			String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
			List<StockPrice> newData = downloadStockPriceAndVolume(symbol, firstDayOfTheMonth);
			return stockPriceDAO.update(symbol, existingData, newData);
		} else {
			return existingData;
		}
	}
	/*
	 * download new price data for all stock items.
	 * This may be call at the end of month.
	 */
	public void updatePriceDataForAllExistSymbols() throws StockException {
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		List<String> symbols = stockPriceDAO.existingSymbols();
		List<StockPrice> spList;
		for (String symbol : symbols) {
			logger.info("update price data for symbol: " + symbol);
			spList = downloadStockPriceAndVolume(symbol, firstDayOfTheMonth);
			stockPriceDAO.update(symbol, spList);
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
		}
	}

	/*
	 * the method download and save stock prices for a collection of stocks.
	 */
	private void preparePriceDataForSymbols(Collection<String> stockSymbols) throws StockException {
		List<String> dateList = StockUtils.calculateDateStringPastSixMonth();
		List<StockPrice> spList = null;
		for (String stockSymbol : stockSymbols) {
			logger.info("prepare price data for symbol:" + stockSymbol);
			spList = new ArrayList<>();
			for (String dateString : dateList) {
				spList.addAll(downloadStockPriceAndVolume(stockSymbol, dateString));
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			}
			stockPriceDAO.save(stockSymbol, spList);
		}
	}

	/*
	 * This method download and save a list of StockPrice for a stock within a month
	 */
	private List<StockPrice> downloadStockPriceAndVolume(String stockSymbol, String dateString) throws StockException {
		try {
			List<StockPrice> spList = new ArrayList<>();
			Document doc = Jsoup
					.connect(String.format(TWSE_INDIVIDUAL_STOCK_DAILY_TRADING_GET_URL, dateString, stockSymbol)).get();
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				StockPrice sp = new StockPrice();
				// some stock price data may contain empty values, so we need to skip them
				try {
					sp.setTradingDateAsString(tds.get(0).text());
					sp.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
					sp.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					sp.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					sp.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					sp.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					sp.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
					sp.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
					spList.add(sp);
				} catch (Exception ex) {
					logger.warn("Stock price for symbol:" + stockSymbol + " and trading date:" + sp.getTradingDate()
							+ " is skipped.");
				}

			}
			return spList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
}

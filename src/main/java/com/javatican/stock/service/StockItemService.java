package com.javatican.stock.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemLogDAO;
import com.javatican.stock.dao.StockItemWeeklyDataDAO;
import com.javatican.stock.dao.StockPriceDAO;
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.WeeklyStockPriceDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockItemLog;
import com.javatican.stock.model.StockItemWeeklyData;
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

	// Urls for downloading stock profile(name, capital)
	private static final String TWSE_STOCK_PROFILE_GET_URL = "http://mops.twse.com.tw/mops/web/t05st03";
	private static final String TWSE_STOCK_PROFILE_POST_URL = "http://mops.twse.com.tw/mops/web/ajax_t05st03";
	// Url for download all stock price ohlc data for the specified date
	private static final String TWSE_TRADING_PRICE_GET_URL = "http://www.tse.com.tw/exchangeReport/MI_INDEX?response=html&date=%s&type=ALLBUT0999";

	@Autowired
	StockConfig stockConfig;
	@Autowired
	StockPriceDAO stockPriceDAO;
	@Autowired
	WeeklyStockPriceDAO weeklyStockPriceDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockItemLogDAO stockItemLogDAO;
	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	StockItemWeeklyDataDAO stockItemWeeklyDataDAO;
	@Autowired
	StockItemHelper stockItemHelper;

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
	 * the method download and save stock prices （last 6 month range） for a
	 * collection of stocks.
	 */
	private void preparePriceDataForSymbols(Collection<String> stockSymbols) throws StockException {
		List<String> dateList = StockUtils.calculateDateStringPastSixMonth();
		List<StockPrice> spList = null;
		for (String stockSymbol : stockSymbols) {
			// not to deal with IX0001(major index), IX0027(electric index),
			// IX0039(financial index), TXFI8(future index)
			if (stockSymbol.startsWith("IX") || stockSymbol.startsWith("TXF"))
				continue;
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
			stockItemHelper.updatePriceDateForItem(stockSymbol, spList.get(spList.size() - 1).getTradingDate());
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
			ex.printStackTrace();
			throw new StockException(ex);
		}
	}

	public StockItem getOrCreate(String symbol) throws StockException {
		StockItem si = stockItemDAO.findBySymbol(symbol);
		if (si == null) {
			si = downloadAndSaveStockProfile(symbol);
		}
		return si;
	}

	public StockItem getStockItem(String symbol) {
		return stockItemDAO.findBySymbol(symbol);
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
			Iterable<StockItem> updatedIter = stockItemDAO.saveAll(siList);
			// create StockItemLog entities
			List<StockItemLog> silList = new ArrayList<>();
			StockItemLog sil;
			for (StockItem sic : updatedIter) {
				sil = new StockItemLog();
				sil.setSymbol(sic.getSymbol());
				sil.setStockItem(sic);
				silList.add(sil);
			}
			stockItemLogDAO.saveAll(silList);
			return updatedIter;
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

	/*
	 * update the price field in stockItem for all items.
	 */
	public void updateStockItemPriceFieldForAll() {
		List<StockItem> siList = stockItemDAO.findAll();
		updatePriceFieldOfStockItems(siList);
	}

	/*
	 * Calculate the average closing price for the last month for the stock item.
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
			if (si.getSymbol().startsWith("IX") || si.getSymbol().startsWith("TXF"))
				continue;
			try {
				List<StockPrice> spList = stockPriceDAO.loadBetweenDate(si.getSymbol(), start, end);
				double average = StockUtils
						.roundDoubleDp2(spList.stream().mapToDouble(StockPrice::getClose).average().getAsDouble());
				// call the setter method, no need to call save()
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
	 * before. The duplicate ones will be checked in StockPriceDAO.update() method.
	 * 
	 * this method shall be run when newest price data is required on-demand
	 */
	public void updatePriceDataForSymbol(String stockSymbol) throws StockException {
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		List<StockPrice> spList = downloadStockPriceAndVolume(stockSymbol, firstDayOfTheMonth);
		List<StockPrice> result = stockPriceDAO.update(stockSymbol, spList);
		//
		stockItemHelper.updatePriceDateForItem(stockSymbol, stockPriceDAO.getLatestDateForPriceData(result));
	}

	/*
	 * get the latest stock price data for the specified stock item. It will try to
	 * download any new price data if available. TODO: Not yet called.
	 */
	public List<StockPrice> getLatestStockPrices(String symbol) throws StockException {
		Date latestTradingDate = tradingDateDAO.getLatestTradingDate();
		List<StockPrice> existingData = stockPriceDAO.load(symbol);
		Date latestDateForPriceData = stockPriceDAO.getLatestDateForPriceData(existingData);
		if (latestTradingDate.after(latestDateForPriceData)) {
			// new data available, so update prices
			String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
			List<StockPrice> newData = downloadStockPriceAndVolume(symbol, firstDayOfTheMonth);
			List<StockPrice> result = stockPriceDAO.update(symbol, existingData, newData);
			//
			stockItemHelper.updatePriceDateForItem(symbol, stockPriceDAO.getLatestDateForPriceData(result));
			return result;
		} else {
			return existingData;
		}
	}

	/*
	 * download new price data for all stock items. This may be call at the end of
	 * month.
	 */
	public void updatePriceDataForAll() throws StockException {
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		Date latestTradingDate = tradingDateDAO.getLatestTradingDate();
		List<StockItemLog> silList = stockItemLogDAO.findByPriceDateBeforeOrIsNull(latestTradingDate);
		//
		List<StockPrice> spList;
		for (StockItemLog sil : silList) {
			if (sil.getSymbol().startsWith("IX") || sil.getSymbol().startsWith("TXF"))
				continue;
			logger.info("update price data for symbol: " + sil.getSymbol());
			spList = downloadStockPriceAndVolume(sil.getSymbol(), firstDayOfTheMonth);
			stockPriceDAO.update(sil.getSymbol(), spList);
			//
			stockItemHelper.updatePriceDateForItem(sil.getSymbol(), spList.get(spList.size() - 1).getTradingDate());
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
		}
	}

	public void updatePriceDataForAllImproved() throws StockException {
		// String firstDayOfTheMonth = "20181101";
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		List<Date> tdList = tradingDateDAO.findLatestNTradingDateDesc(2);
		Date latestTradingDate = tdList.get(0);
		Date penultimateTradingDate = tdList.get(1);
		//
		List<StockItemLog> silList = stockItemLogDAO.findByPriceDateBeforeOrIsNull(latestTradingDate);
		Map<String, StockPrice> latestSpMap = downloadAllStockPricesForDate(latestTradingDate);
		//
		List<StockPrice> existingSpList;
		for (StockItemLog sil : silList) {
			if (sil.getSymbol().startsWith("IX") || sil.getSymbol().startsWith("TXF"))
				continue;
			logger.info("updating price data for symbol: " + sil.getSymbol());
			//
			existingSpList = stockPriceDAO.load(sil.getSymbol());
			if (stockPriceDAO.getLatestDateForPriceData(existingSpList).compareTo(penultimateTradingDate) == 0) {
				StockPrice sp = latestSpMap.get(sil.getSymbol());
				if (sp == null)
					continue;
				existingSpList.add(sp);
				stockPriceDAO.save(sil.getSymbol(), existingSpList);
				stockItemHelper.updatePriceDateForItem(sil.getSymbol(), sp.getTradingDate());
			} else {
				List<StockPrice> spList = downloadStockPriceAndVolume(sil.getSymbol(), firstDayOfTheMonth);
				if (spList.size() == 0) {
					logger.info("no price data for symbol: " + sil.getSymbol());
					try {
						Thread.sleep(stockConfig.getSleepTime());
					} catch (InterruptedException ex) {
					}
					continue;
				}
				stockPriceDAO.update(sil.getSymbol(), existingSpList, spList);
				stockItemHelper.updatePriceDateForItem(sil.getSymbol(), spList.get(spList.size() - 1).getTradingDate());
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			}
			logger.info("finish updating price data for symbol: " + sil.getSymbol());
		}
	}

	private Map<String, StockPrice> downloadAllStockPricesForDate(Date tradingDate) throws StockException {
		String dateString = StockUtils.dateToSimpleString(tradingDate);
		Map<String, StockPrice> spMap = new TreeMap<>();
		String strUrl = String.format(TWSE_TRADING_PRICE_GET_URL, dateString);
		try (InputStream inStream = new URL(strUrl).openStream();) {
			Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
			Elements tables = doc.select("body > div > table");
			Elements trs = tables.get(4).select("tbody > tr");
			StockPrice sp;
			String symbol;
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				sp = new StockPrice();
				symbol = tds.get(0).text();
				sp.setTradingDate(tradingDate);
				try {
					sp.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					sp.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					sp.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					sp.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					sp.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
					sp.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text())));
					sp.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
					// add to map
					spMap.put(symbol, sp);
				} catch (Exception ex) {
					logger.warn("Problem parsing the price data for stock symbol: " + symbol + ". Igore it.");
				}
			}
		} catch (Exception ex) {
			throw new StockException(ex);
		}
		return spMap;

	}

	/*
	 * Calculate RSV and KD values for all items ( filtered on the statsDate is null
	 * or before latestTradingDate)
	 */
	public void calculateAndSaveKDForAll() throws StockException {

		Date latestTradingDate = tradingDateDAO.getLatestTradingDate();
		List<StockItemLog> silList = stockItemLogDAO.findByStatsDateBeforeOrIsNull(latestTradingDate);
		for (StockItemLog sil : silList) {
			if (sil.getSymbol().startsWith("IX") || sil.getSymbol().startsWith("TXF"))
				continue;
			Date latestDate = calculateAndSaveKDForSymbol(sil.getSymbol());
			// stockItemHelper.updateStatsDateForItem(sil, latestDate);
			stockItemHelper.updateStatsDateForItem(sil.getSymbol(), latestDate);
		}
	}

	public void calculateAndSaveWeeklyKDForAll() throws StockException {
		List<StockItem> siList = stockItemDAO.findAll();
		for (StockItem si : siList) {
			calculateAndSaveWeeklyKDForSymbol(si.getSymbol());
		}
	}

	/*
	 * Calculate RSV and KD values for the specified stock item
	 */
	public Date calculateAndSaveKDForSymbol(String symbol) throws StockException {
		List<StockPrice> spList = stockPriceDAO.load(symbol);
		Date latestDate = spList.get(spList.size() - 1).getTradingDate();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		stats.setWindowSize(9);
		//
		List<StockItemData> sidList = new ArrayList<>();
		spList.stream().forEach(sp -> sidList.add(new StockItemData(sp.getTradingDate(), sp)));

		sidList.stream().forEach(sid -> {
			stats.addValue(sid.getStockPrice().getHigh());
			sid.setHigh(stats.getMax());
		});
		stats.clear();
		sidList.stream().forEach(sid -> {
			stats.addValue(sid.getStockPrice().getLow());
			sid.setLow(stats.getMin());
		});
		sidList.stream().forEach(sid -> {
			if (sid.getHigh() - sid.getLow() < 0.001) {
				sid.setRsv(100.0);
			} else {
				sid.setRsv(StockUtils.roundDoubleDp2(
						100 * (sid.getStockPrice().getClose() - sid.getLow()) / (sid.getHigh() - sid.getLow())));
			}
		});
		IntStream.range(0, sidList.size()).forEach(idx -> {
			StockItemData sid = sidList.get(idx);
			double k;
			double d;
			if (idx == 0) {
				k = 50.0 * 2 / 3 + sid.getRsv() / 3;
				d = 50.0 * 2 / 3 + k / 3.0;
			} else {
				k = sidList.get(idx - 1).getK() * 2 / 3 + sid.getRsv() / 3.0;
				d = sidList.get(idx - 1).getD() * 2 / 3 + k / 3;
			}
			sid.setK(StockUtils.roundDoubleDp2(k));
			sid.setD(StockUtils.roundDoubleDp2(d));
		});
		// simple moving average
		CircularFifoQueue<Double> queue = new CircularFifoQueue<>(60);
		// CircularFifoQueue<Double> queue = new
		// CircularFifoQueue<>(Arrays.asList(ArrayUtils.toObject(new double[60])));
		sidList.stream().forEach(sid -> {
			queue.add(sid.getStockPrice().getClose());
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			if (values.length == 60) {
				sid.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, 55, 5)));
				sid.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, 50, 10)));
				sid.setSma20(StockUtils.roundDoubleDp2(StatUtils.mean(values, 40, 20)));
				sid.setSma60(StockUtils.roundDoubleDp2(StatUtils.mean(values, 0, 60)));
			} else if (values.length >= 20) {
				sid.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
				sid.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 10, 10)));
				sid.setSma20(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 20, 20)));
			} else if (values.length >= 10) {
				sid.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
				sid.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 10, 10)));
			} else if (values.length >= 5) {
				sid.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
			}
		});
		stockItemDataDAO.save(symbol, sidList);
		return latestDate;
	}

	/*
	 * Calculate RSV and KD values for the specified stock item
	 */
	public void calculateAndSaveWeeklyKDForSymbol(String symbol) {
		List<StockPrice> spList = null;
		try {
			spList = weeklyStockPriceDAO.load(symbol);
		} catch (Exception ex) {
			logger.warn("Error loading weekly price data for symbol:" + symbol);
			return;
		}
		// Date latestDate = spList.get(spList.size() - 1).getTradingDate();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		stats.setWindowSize(9);
		//
		List<StockItemWeeklyData> siwdList = new ArrayList<>();
		spList.stream().forEach(sp -> siwdList.add(new StockItemWeeklyData(sp.getTradingDate(), sp)));

		siwdList.stream().forEach(siwd -> {
			stats.addValue(siwd.getStockPrice().getHigh());
			siwd.setHigh(stats.getMax());
		});
		stats.clear();
		siwdList.stream().forEach(siwd -> {
			stats.addValue(siwd.getStockPrice().getLow());
			siwd.setLow(stats.getMin());
		});
		siwdList.stream().forEach(siwd -> {
			if (siwd.getHigh() - siwd.getLow() < 0.001) {
				siwd.setRsv(100.0);
			} else {
				siwd.setRsv(StockUtils.roundDoubleDp2(
						100 * (siwd.getStockPrice().getClose() - siwd.getLow()) / (siwd.getHigh() - siwd.getLow())));
			}
		});
		IntStream.range(0, siwdList.size()).forEach(idx -> {
			StockItemWeeklyData siwd = siwdList.get(idx);
			double k;
			double d;
			if (idx == 0) {
				k = 50.0 * 2 / 3 + siwd.getRsv() / 3;
				d = 50.0 * 2 / 3 + k / 3.0;
			} else {
				k = siwdList.get(idx - 1).getK() * 2 / 3 + siwd.getRsv() / 3.0;
				d = siwdList.get(idx - 1).getD() * 2 / 3 + k / 3;
			}
			siwd.setK(StockUtils.roundDoubleDp2(k));
			siwd.setD(StockUtils.roundDoubleDp2(d));
		});
		// simple moving average
		CircularFifoQueue<Double> queue = new CircularFifoQueue<>(24);
		// CircularFifoQueue<Double> queue = new
		// CircularFifoQueue<>(Arrays.asList(ArrayUtils.toObject(new double[60])));
		siwdList.stream().forEach(siwd -> {
			queue.add(siwd.getStockPrice().getClose());
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			if (values.length == 24) {
				siwd.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, 20, 4)));
				siwd.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, 16, 8)));
				siwd.setSma12(StockUtils.roundDoubleDp2(StatUtils.mean(values, 12, 12)));
				siwd.setSma24(StockUtils.roundDoubleDp2(StatUtils.mean(values, 0, 24)));
			} else if (values.length >= 12) {
				siwd.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
				siwd.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 8, 8)));
				siwd.setSma12(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 12, 12)));
			} else if (values.length >= 8) {
				siwd.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
				siwd.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 8, 8)));
			} else if (values.length >= 4) {
				siwd.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
			}
		});
		try {
			stockItemWeeklyDataDAO.save(symbol, siwdList);
		} catch (StockException e) {
			logger.warn("Error saving weekly stats data for symbol:" + symbol);
		}
	}
	// public void migrateData() {
	// List<StockItem> siList = stockItemDAO.findAll();
	// List<StockItemLog> silList = new ArrayList<>();
	// for(StockItem si: siList) {
	// StockItemLog sil = new StockItemLog();
	// sil.setSymbol(si.getSymbol());
	// sil.setChartDate(si.getChartDate());
	// sil.setPriceDate(si.getPriceDate());
	// sil.setStatsDate(si.getStatsDate());
	// sil.setStockItem(si);
	// silList.add(sil);
	// }
	// stockItemLogDAO.saveAll(silList);
	// }

	public void prepareWeeklyStockPriceForAll() {
		List<Date> tdList = tradingDateDAO.findAllTradingDate();
		List<String> symbolList = stockItemDAO.getAllSymbols();
		// weeklySpMap: key is the dateString , value is a StockPrice (store weekly
		// price)
		TreeMap<String, StockPrice> weeklySpMap;
		for (String symbol : symbolList) {
			weeklySpMap = new TreeMap<>();
			List<StockPrice> spList;
			// load daily price
			try {
				spList = stockPriceDAO.load(symbol);
			} catch (StockException e) {
				logger.warn("Error loading stock price for symbol:" + symbol);
				continue;
			}
			for (StockPrice sp : spList) {
				// get its last day of the week(not necessarily Friday, because of holidays)
				// logger.info(sp.toString());
				Date lastTradingDateOfWeek = StockUtils.getLastTradingDateOfWeek(sp.getTradingDate(), tdList);
				if (lastTradingDateOfWeek == null)
					continue;
				String dateString = StockUtils.dateToSimpleString(lastTradingDateOfWeek);
				StockPrice weeklySp = weeklySpMap.get(dateString);
				if (weeklySp == null) {
					weeklySp = new StockPrice();
					weeklySpMap.put(dateString, weeklySp);
					weeklySp.setTradingDate(lastTradingDateOfWeek);
					// set open/high/low prices
					weeklySp.setOpen(sp.getOpen());
					weeklySp.setHigh(sp.getHigh());
					weeklySp.setLow(sp.getLow());
				}
				if (sp.getTradingDate().getTime() == lastTradingDateOfWeek.getTime()) {
					// set close price
					weeklySp.setClose(sp.getClose());
				}
				// update other fields except open/close
				updateWeeklyPrice(weeklySp, sp);
			}
			// save weekly price for item
			try {
				weeklyStockPriceDAO.save(symbol, new ArrayList<>(weeklySpMap.values()));
			} catch (StockException e) {
				logger.warn("Error saving weekly stock price for symbol:" + symbol);
				continue;
			}

		}
	}

	/*
	 * update values except open and close price fields
	 * 
	 */
	private void updateWeeklyPrice(StockPrice weeklySp, StockPrice sp) {
		if (sp.getHigh() > weeklySp.getHigh()) {
			weeklySp.setHigh(sp.getHigh());
		}
		if (sp.getLow() < weeklySp.getLow()) {
			weeklySp.setLow(sp.getLow());
		}
		weeklySp.setTransaction(weeklySp.getTransaction() + sp.getTransaction());
		weeklySp.setTradeVolume(weeklySp.getTradeVolume() + sp.getTradeVolume());
		weeklySp.setTradeValue(weeklySp.getTradeValue() + sp.getTradeValue());
	}

	public List<String> getAllSymbols() {
		return stockItemDAO.getAllSymbols();
	}

	public List<StockItem> findAllStockItems() {
		return stockItemDAO.findAll();
	}

	public Map<String, StockItem> findAllStockItemsAsMap() {
		return stockItemDAO.findAllAsMap();
	}
}

package com.javatican.stock.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockPriceChangeDAO;
import com.javatican.stock.dao.StockTradeByForeignDAO;
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.dao.WeeklyTradingValueDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.model.WeeklyTradingValue;
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
	private static final String TWSE_TRADING_INDEX_GET_URL = "http://www.tse.com.tw/indicesReport/MI_5MINS_HIST?response=html&date=%s";
	private static final String TWSE_TRADING_PRICE_GET_URL = "http://www.tse.com.tw/exchangeReport/MI_INDEX?response=html&date=%s&type=ALLBUT0999";
	private static final String TWSE_TRADING_MARGIN_GET_URL = "http://www.tse.com.tw/exchangeReport/MI_MARGN?response=html&date=%s&selectType=MS";
	@Autowired
	StockConfig stockConfig;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	TradingValueDAO tradingValueDAO;
	@Autowired
	WeeklyTradingValueDAO weeklyTradingValueDAO;
	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	@Autowired
	StockTradeByForeignDAO stockTradeByForeignDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	StockPriceChangeDAO stockPriceChangeDAO;
	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;
	@Autowired
	PutWarrantTradeSummaryDAO putWarrantTradeSummaryDAO;
	@Autowired
	StockItemService stockItemService;
	@Autowired
	StockItemHelper stockItemHelper;

	public long tradingDateCount() {
		return tradingDateDAO.count();
	}

	/*
	 * update trading dates and total trading values and trading values for 3 big
	 * investors. The download contains the data for the current month, so
	 * duplicates are checked before save into db
	 * 
	 * This shall be run after every trading date.
	 */
	public Date updateTradingDateAndValue() throws StockException {
		String dateString = StockUtils.todayDateString();
		return downloadAndSaveTradingDateAndValueForTheMonth(dateString, true);
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
	 * month. It calls setForeignAndOtherInvestorsTradingValue() return the latest
	 * trading date.
	 */
	private Date downloadAndSaveTradingDateAndValueForTheMonth(String dateString, boolean checkDuplicate)
			throws StockException {
		try {
			Date latestTradingDate = null;
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
					latestTradingDate = date;
					TradingDate tDate = new TradingDate();
					tDate.setDate(date);
					tradingDateDAO.save(tDate);

					Elements tds = tr.select("td");
					//
					TradingValue tValue = new TradingValue();
					tValue.setTradingDate(date);
					tValue.setTotalValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					// set trading values for foreign and other investors
					String dString = StockUtils.dateToSimpleString(tDate.getDate());
					setForeignAndOtherInvestorsTradingValue(dString, tValue);
					downloadAndSaveIndexOhlcData(dString, tValue);
					downloadAndSaveMargin(dString, tValue);
					tradingValueDAO.save(tValue);
					try {
						Thread.sleep(stockConfig.getSleepTime());
					} catch (InterruptedException ex) {
					}
				}
			}
			return latestTradingDate;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new StockException(ex);
		}
	}

	/*
	 * download and store the index ohlc data for a specific month.
	 */

	private void downloadAndSaveIndexOhlcData(String dateString, TradingValue tValue) throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_TRADING_INDEX_GET_URL, dateString)).get();
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Element td = tr.selectFirst("td");
				Date date = StockUtils.rocYearStringToDate(td.text()).get();
				if (!StockUtils.dateToSimpleString(date).equals(dateString)) {
					continue;
				} else {
					Elements tds = tr.select("td");
					tValue.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
					tValue.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					tValue.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					tValue.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					break;
				}
			}
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	private void downloadAndSaveMargin(String dateString, TradingValue tValue) throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_TRADING_MARGIN_GET_URL, dateString)).get();
			Elements trs = doc.select("table > tbody > tr");
			Element tr = trs.get(0);
			Elements tds = tr.select("td");
			//
			tValue.setMarginBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text()))
					- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			tValue.setMarginRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			tValue.setMarginAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
			//
			tr = trs.get(1);
			tds = tr.select("td");
			tValue.setShortRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
			tValue.setShortSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text()))
					- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			tValue.setShortAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
			//
			//
			tr = trs.get(2);
			tds = tr.select("td");
			// unit is NTD 1000
			tValue.setMarginBuyValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text()))
					- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
			tValue.setMarginRedempValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
			tValue.setMarginAccValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new StockException(ex);
		}
	}

	/*
	 * download and store the index ohlc data for a specific month.
	 */
	// public void downloadAndSaveIndexOhlcDataForTheMonth(String dateString) throws
	// StockException {
	// try {
	// Document doc = Jsoup.connect(String.format(TWSE_TRADING_INDEX_GET_URL,
	// dateString)).get();
	// Elements trs = doc.select("table > tbody > tr");
	// for (Element tr : trs) {
	//
	// Elements tds = tr.select("td");
	// Date date = StockUtils.rocYearStringToDate(tds.get(0).text()).get();
	// //
	// TradingValue tValue = tradingValueDAO.getByTradingDate(date);
	// if (tValue == null)
	// continue;
	// tValue.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
	// tValue.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
	// tValue.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
	// tValue.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
	// tradingValueDAO.save(tValue);
	//
	// }
	// } catch (Exception ex) {
	// throw new StockException(ex);
	// }
	// }
	/*
	 * below are called once.
	 */
	private void downloadAndSaveMargin() throws StockException {
		try {
			List<Date> tradingDateList = tradingDateDAO.findAllTradingDate();
			for (Date date : tradingDateList) {
				String dateString = StockUtils.dateToSimpleString(date);

				Document doc = Jsoup.connect(String.format(TWSE_TRADING_MARGIN_GET_URL, dateString)).get();
				Elements trs = doc.select("table > tbody > tr");
				Element tr = trs.get(0);
				Elements tds = tr.select("td");

				//
				TradingValue tValue = tradingValueDAO.getByTradingDate(date);
				if (tValue == null)
					continue;
				tValue.setMarginBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text()))
						- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
				tValue.setMarginRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				tValue.setMarginAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				//
				tr = trs.get(1);
				tds = tr.select("td");
				tValue.setShortRedemp(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
				tValue.setShortSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text()))
						- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
				tValue.setShortAcc(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				//
				//
				tr = trs.get(2);
				tds = tr.select("td");
				// unit is NTD 1000
				tValue.setMarginBuyValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text()))
						- Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
				tValue.setMarginRedempValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				tValue.setMarginAccValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));

				tradingValueDAO.save(tValue);
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
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
	 * Calculate RSV and KD values for the specified stock item
	 */
	public void calculateIndexStatsData() {
		List<TradingValue> tvList = tradingValueDAO.findAll();
		List<TradingValue> updateList = new ArrayList<>();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		stats.setWindowSize(9);
		//

		tvList.stream().forEach(tv -> {
			stats.addValue(tv.getHigh());
			if (tv.getMax9() == null) {
				tv.setMax9(stats.getMax());
				logger.info("modify index stats data for" + tv.getTradingDate());
				updateList.add(tv);
			}
		});
		stats.clear();
		tvList.stream().forEach(tv -> {
			stats.addValue(tv.getLow());
			if (tv.getMin9() == null) {
				tv.setMin9(stats.getMin());
			}
		});
		tvList.stream().forEach(tv -> {
			if (tv.getRsv() == null) {
				if (tv.getMax9() - tv.getMin9() < 0.001) {
					tv.setRsv(100.0);
				} else {
					tv.setRsv(StockUtils
							.roundDoubleDp2(100 * (tv.getClose() - tv.getMin9()) / (tv.getMax9() - tv.getMin9())));
				}
			}
		});
		IntStream.range(0, tvList.size()).forEach(idx -> {
			TradingValue tv = tvList.get(idx);
			if (tv.getK() == null) {
				double k;
				double d;
				if (idx == 0) {
					k = 50.0 * 2 / 3 + tv.getRsv() / 3;
					d = 50.0 * 2 / 3 + k / 3.0;
				} else {
					k = tvList.get(idx - 1).getK() * 2 / 3 + tv.getRsv() / 3.0;
					d = tvList.get(idx - 1).getD() * 2 / 3 + k / 3;
				}
				tv.setK(StockUtils.roundDoubleDp2(k));
				tv.setD(StockUtils.roundDoubleDp2(d));
			}
		});
		// simple moving average
		CircularFifoQueue<Double> queue = new CircularFifoQueue<>(60);
		// CircularFifoQueue<Double> queue = new
		// CircularFifoQueue<>(Arrays.asList(ArrayUtils.toObject(new double[60])));
		tvList.stream().forEach(tv -> {
			queue.add(tv.getClose());
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			if (tv.getSma5() == null) {
				if (values.length == 60) {
					tv.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, 55, 5)));
					tv.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, 50, 10)));
					tv.setSma20(StockUtils.roundDoubleDp2(StatUtils.mean(values, 40, 20)));
					tv.setSma60(StockUtils.roundDoubleDp2(StatUtils.mean(values, 0, 60)));
				} else if (values.length >= 20) {
					tv.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
					tv.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 10, 10)));
					tv.setSma20(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 20, 20)));
				} else if (values.length >= 10) {
					tv.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
					tv.setSma10(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 10, 10)));
				} else if (values.length >= 5) {
					tv.setSma5(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 5, 5)));
				}
			}
		});
		tradingValueDAO.saveAll(updateList);
	}
//
	public void calculateWeeklyIndexStatsData() {
		List<Date> tdList = tradingDateDAO.findAllTradingDate();
		// weeklySpMap: key is the dateString , value is a StockPrice (store weekly
		// price)
		TreeMap<String, WeeklyTradingValue> weeklyTvMap = new TreeMap<>();
		List<TradingValue> tvList;
		// load tradingValue
		tvList = tradingValueDAO.findAll();
		for (TradingValue tv : tvList) {
			// get its last day of the week(not necessarily Friday, because of holidays)
			// logger.info(tv.toString());
			Date lastTradingDateOfWeek = StockUtils.getLastTradingDateOfWeek(tv.getTradingDate(), tdList);
			String dateString = StockUtils.dateToSimpleString(lastTradingDateOfWeek);
			WeeklyTradingValue weeklyTv = weeklyTvMap.get(dateString);
			if (weeklyTv == null) {
				weeklyTv = new WeeklyTradingValue();
				weeklyTvMap.put(dateString, weeklyTv);
				weeklyTv.setTradingDate(lastTradingDateOfWeek);
				// set open/high/low prices
				weeklyTv.setOpen(tv.getOpen());
				weeklyTv.setHigh(tv.getHigh());
				weeklyTv.setLow(tv.getLow());
			}
			//compare equalness using time value instead of using Date object directly, because Date object shows unequalness.
			if (tv.getTradingDate().getTime() == lastTradingDateOfWeek.getTime()) {
				// set close price
				weeklyTv.setClose(tv.getClose());
			}
			// update other fields except open/close
			updateWeeklyData(weeklyTv, tv);
		}
		//
		List<WeeklyTradingValue> wtvList = new ArrayList<>(weeklyTvMap.values());
		DescriptiveStatistics stats = new DescriptiveStatistics();
		stats.setWindowSize(9);

		wtvList.stream().forEach(wtv -> {
			stats.addValue(wtv.getHigh());
			wtv.setMax9(stats.getMax());
		});
		stats.clear();
		wtvList.stream().forEach(wtv -> {
			stats.addValue(wtv.getLow());
			wtv.setMin9(stats.getMin());
		});
		wtvList.stream().forEach(wtv -> {
			if (wtv.getMax9() - wtv.getMin9() < 0.001) {
				wtv.setRsv(100.0);
			} else {
				wtv.setRsv(StockUtils
						.roundDoubleDp2(100 * (wtv.getClose() - wtv.getMin9()) / (wtv.getMax9() - wtv.getMin9())));
			}
		});
		IntStream.range(0, wtvList.size()).forEach(idx -> {
			WeeklyTradingValue wtv = wtvList.get(idx);
			double k;
			double d;
			if (idx == 0) {
				k = 50.0 * 2 / 3 + wtv.getRsv() / 3;
				d = 50.0 * 2 / 3 + k / 3.0;
			} else {
				k = wtvList.get(idx - 1).getK() * 2 / 3 + wtv.getRsv() / 3.0;
				d = wtvList.get(idx - 1).getD() * 2 / 3 + k / 3;
			}
			wtv.setK(StockUtils.roundDoubleDp2(k));
			wtv.setD(StockUtils.roundDoubleDp2(d));
		});
		// simple moving average
		CircularFifoQueue<Double> queue = new CircularFifoQueue<>(24);
		wtvList.stream().forEach(wtv -> {
			queue.add(wtv.getClose());
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			if (values.length == 24) {
				wtv.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, 20, 4)));
				wtv.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, 16, 8)));
				wtv.setSma12(StockUtils.roundDoubleDp2(StatUtils.mean(values, 12, 12)));
				wtv.setSma24(StockUtils.roundDoubleDp2(StatUtils.mean(values, 0, 24)));
			} else if (values.length >= 12) {
				wtv.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
				wtv.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 8, 8)));
				wtv.setSma12(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 12, 12)));
			} else if (values.length >= 8) {
				wtv.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
				wtv.setSma8(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 8, 8)));
			} else if (values.length >= 4) {
				wtv.setSma4(StockUtils.roundDoubleDp2(StatUtils.mean(values, values.length - 4, 4)));
			}
		});
		List<WeeklyTradingValue> existingWtvList = weeklyTradingValueDAO.findAll();
		Iterator<WeeklyTradingValue> itr = existingWtvList.iterator();
		while (itr.hasNext()) {
			WeeklyTradingValue wtv2 = itr.next();
			String key = StockUtils.dateToSimpleString(wtv2.getTradingDate());
			if (weeklyTvMap.containsKey(key)) {
				weeklyTvMap.remove(key);
				// itr.remove() will remove the current iterated item from existingWtvList
				itr.remove();
			}
		}
		// first remove the last item that is not the final 'week' stats data.
		weeklyTradingValueDAO.deleteAll(existingWtvList);
		// insert any new 'week' stats data
		weeklyTradingValueDAO.saveAll(weeklyTvMap.values());
	}

	/*
	 * update values except open and close price fields
	 * 
	 */
	private void updateWeeklyData(WeeklyTradingValue weeklyTv, TradingValue tv) {
		if (tv.getHigh() > weeklyTv.getHigh()) {
			weeklyTv.setHigh(tv.getHigh());
		}
		if (tv.getLow() < weeklyTv.getLow()) {
			weeklyTv.setLow(tv.getLow());
		}
		weeklyTv.setTotalValue(weeklyTv.getTotalValue() + tv.getTotalValue());
		weeklyTv.setDealerBuy(weeklyTv.getDealerBuy() + tv.getDealerBuy());
		weeklyTv.setDealerSell(weeklyTv.getDealerSell() + tv.getDealerSell());
		weeklyTv.setDealerDiff(weeklyTv.getDealerDiff() + tv.getDealerDiff());
		weeklyTv.setDealerHedgeBuy(weeklyTv.getDealerHedgeBuy() + tv.getDealerHedgeBuy());
		weeklyTv.setDealerHedgeSell(weeklyTv.getDealerHedgeSell() + tv.getDealerHedgeSell());
		weeklyTv.setDealerHedgeDiff(weeklyTv.getDealerHedgeDiff() + tv.getDealerHedgeDiff());
		weeklyTv.setForeignBuy(weeklyTv.getForeignBuy() + tv.getForeignBuy());
		weeklyTv.setForeignSell(weeklyTv.getForeignSell() + tv.getForeignSell());
		weeklyTv.setForeignDiff(weeklyTv.getForeignDiff() + tv.getForeignDiff());
		weeklyTv.setTrustBuy(weeklyTv.getTrustBuy() + tv.getTrustBuy());
		weeklyTv.setTrustSell(weeklyTv.getTrustSell() + tv.getTrustSell());
		weeklyTv.setTrustDiff(weeklyTv.getTrustDiff() + tv.getTrustDiff());
		
		weeklyTv.setMarginBuy(weeklyTv.getMarginBuy() + tv.getMarginBuy());
		weeklyTv.setMarginRedemp(weeklyTv.getMarginRedemp() + tv.getMarginRedemp());
		weeklyTv.setMarginAcc(tv.getMarginAcc());
		
		weeklyTv.setMarginBuyValue(weeklyTv.getMarginBuyValue() + tv.getMarginBuyValue());
		weeklyTv.setMarginRedempValue(weeklyTv.getMarginRedempValue() + tv.getMarginRedempValue());
		weeklyTv.setMarginAccValue(tv.getMarginAccValue());

		weeklyTv.setShortSell(weeklyTv.getShortSell() + tv.getShortSell());
		weeklyTv.setShortRedemp(weeklyTv.getShortRedemp() + tv.getShortRedemp());
		weeklyTv.setShortAcc(tv.getShortAcc());
	}

	public List<Date> getLatestNTradingDateDesc(int dateLength) {
		return tradingDateDAO.findLatestNTradingDateDesc(dateLength);
	}

	public Date getLatestTradingDate() {
		return tradingDateDAO.getLatestTradingDate();
	}

	public LinkedHashMap<StockItem, TreeMap<String, StockTradeByTrust>> getTopStockItemTradeByTrust(Date tradingDate,
			int dateLength, int selectCount) {
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
		}).limit(selectCount).map(stbt -> stbt.getStockItem()).collect(Collectors.toList());
		// 3. query the latest 10 trading dates
		List<Date> dList = tradingDateDAO.findLatestNTradingDateDesc(dateLength);
		// 4. map object to return to caller
		// key is the stockItem
		// value is a sub-map object with key of trading date and value of
		// StockTradeByTrust object
		LinkedHashMap<StockItem, TreeMap<String, StockTradeByTrust>> dataMap = new LinkedHashMap<>();
		// 5. filter the 'stbt' collection using latest 10 trading dates list.
		// and create the sub-map object
		siList.stream().forEach(si -> {
			TreeMap<String, StockTradeByTrust> map = new TreeMap<>();
			si.getStbt().stream().filter(stbt -> dList.contains(stbt.getTradingDate()))
					.forEach(stbt -> map.put(StockUtils.dateToStringSeparatedBySlash(stbt.getTradingDate()), stbt));
			// map.values().stream().forEach(stbt->logger.info(stbt.toString()));
			dataMap.put(si, map);
		});
		return dataMap;
	}

	public LinkedHashMap<StockItem, TreeMap<String, StockTradeByForeign>> getTopStockItemTradeByForeign(
			Date tradingDate, int dateLength, int selectCount) {
		// 1. get the stbf list for the specified date
		List<StockTradeByForeign> stbfList = stockTradeByForeignDAO.getByTradingDate(tradingDate);
		// 2. sort the stbf list by (buy+sell)*price amount in descending order
		// and select the top items and collect its stockItem relationship objects
		// (note that the stockItem object has its 'stbf' relationship retrieved from
		// DB.)
		List<StockItem> siList = stbfList.stream().sorted((stbf1, stbf2) -> {
			double price1 = stbf1.getStockItem().getPrice();
			double price2 = stbf2.getStockItem().getPrice();
			double amt1 = price1 * (stbf1.getBuy() + stbf1.getSell());
			double amt2 = price2 * (stbf2.getBuy() + stbf2.getSell());
			return -1 * Double.compare(amt1, amt2);
		}).limit(selectCount).map(stbf -> stbf.getStockItem()).collect(Collectors.toList());
		// 3. query the latest 10 trading dates
		List<Date> dList = tradingDateDAO.findLatestNTradingDateDesc(dateLength);
		// 4. map object to return to caller
		// key is the stockItem
		// value is a sub-map object with key of trading date and value of
		// StockTradeByForeign object
		LinkedHashMap<StockItem, TreeMap<String, StockTradeByForeign>> dataMap = new LinkedHashMap<>();
		// 5. filter the 'stbf' collection using latest 10 trading dates list.
		// and create the sub-map object
		siList.stream().forEach(si -> {
			TreeMap<String, StockTradeByForeign> map = new TreeMap<>();
			si.getStbf().stream().filter(stbf -> dList.contains(stbf.getTradingDate()))
					.forEach(stbf -> map.put(StockUtils.dateToStringSeparatedBySlash(stbf.getTradingDate()), stbf));
			dataMap.put(si, map);
		});
		return dataMap;
	}

	public List<StockItemData> getStockItemStatsData(StockItem si) {
		try {
			return stockItemDataDAO.load(si.getSymbol());
		} catch (StockException e) {
			logger.warn("Error loading stock item stats data for symbol:" + si.getSymbol());
			return null;
		}
	}

	public List<StockItemData> getStockItemStatsData(String symbol) {
		try {
			return stockItemDataDAO.load(symbol);
		} catch (StockException e) {
			logger.warn("Error loading stock item stats data for symbol:" + symbol);
			return null;
		}
	}

	public LinkedHashMap<StockItem, TreeMap<String, StockItemData>> getStockItemStatsData(List<StockItem> siList,
			List<Date> dateList) {
		LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = new LinkedHashMap<>();
		siList.stream().forEach(si -> {
			try {
				List<StockItemData> sidList = stockItemDataDAO.load(si.getSymbol());
				TreeMap<String, StockItemData> map = new TreeMap<>();
				sidList.stream().filter(sid -> dateList.contains(sid.getTradingDate()))
						.forEach(sid -> map.put(StockUtils.dateToStringSeparatedBySlash(sid.getTradingDate()), sid));
				statsMap.put(si, map);
			} catch (StockException ex) {
				logger.warn("Can not load the stats data for stock symbol: " + si.getSymbol());
			}

		});
		return statsMap;
	}

	/*
	 * download and store the trading price data (not including warrants) for the
	 * specified trading date.
	 */
	// @SuppressWarnings("unused")
	// public void downloadAndSavePriceData(String dateString) throws StockException
	// {
	// try {
	// Document doc = Jsoup.connect(String.format(TWSE_TRADING_PRICE_GET_URL,
	// dateString)).get();
	//
	// Elements tables = doc.select("body > div > table");
	// Elements trs = tables.get(4).select("tbody > tr");
	// StockPriceChange spc;
	// Date tradingDate = StockUtils.stringSimpleToDate(dateString).get();
	// for (Element tr : trs) {
	// Elements tds = tr.select("td");
	// logger.info("tds size="+tds.size());
	// if(tds.size()!=15) {
	// for(int i=0; i<tds.size(); i++) {
	// logger.info("tds["+i+"]="+tds.get(i).text());
	// }
	// }
	// spc = new StockPriceChange();
	// spc.setSymbol(tds.get(0).text());
	// spc.setTradingDate(tradingDate);
	// //spc.setName(tds.get(1).text());
	// double sign = 1.0;
	// try {
	// String signStr = tds.get(8).selectFirst("p").text();
	// if (signStr.indexOf("-") >= 0)
	// sign = -1.0;
	// } catch (Exception ex) {
	// }
	// try {
	// spc.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
	// spc.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
	// spc.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
	// spc.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
	// spc.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
	// spc.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
	// spc.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text())));
	// spc.setChange(sign *
	// Double.valueOf(StockUtils.removeCommaInNumber(tds.get(9).text())));
	// } catch (Exception ex) {
	// }
	// logger.info(spc.toString());
	// }
	// } catch (Exception ex) {
	// throw new StockException(ex);
	// }
	// }

	/*
	 * download and store the trading price data (not including warrants) for the
	 * specified trading date. Note: while parsing the html document, there are some
	 * bizarre problems with correctly selecting td elements. So here Jsoup.parse()
	 * is used with an InputStream. I suspect it is the encoding problem.
	 */
	private List<StockPriceChange> downloadAndSavePriceData(String dateString) throws StockException {
		List<StockPriceChange> spcList = new ArrayList<>();
		String strUrl = String.format(TWSE_TRADING_PRICE_GET_URL, dateString);
		try (InputStream inStream = new URL(strUrl).openStream();) {
			Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
			Elements tables = doc.select("body > div > table");
			Elements trs = tables.get(4).select("tbody > tr");
			StockPriceChange spc;
			Date tradingDate = StockUtils.stringSimpleToDate(dateString).get();
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				spc = new StockPriceChange();
				spc.setSymbol(tds.get(0).text());
				spc.setTradingDate(tradingDate);
				spc.setName(tds.get(1).text());
				double sign = 1.0;
				try {
					String signStr = tds.get(9).selectFirst("p").text();
					if (signStr.indexOf("-") >= 0)
						sign = -1.0;
				} catch (Exception ex) {
				}
				try {
					spc.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					spc.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					spc.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					spc.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					spc.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
					spc.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text())));
					spc.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
					spc.setChange(sign * Double.valueOf(StockUtils.removeCommaInNumber(tds.get(10).text())));
					// spc.setChangePercent(spc.getChange() / spc.getClose());
					spc.setChangePercent(
							StockUtils.roundDoubleDp4(spc.getChange() / (spc.getClose() - spc.getChange())));
					spcList.add(spc);
				} catch (Exception ex) {
					logger.warn("Problem parsing the price data for stock symbol: " + spc.getSymbol() + ". Igore it.");
				}
			}
		} catch (Exception ex) {
			throw new StockException(ex);
		}
		return spcList;
	}

	public List<StockPriceChange> loadTop(Date tradingDate) throws StockException {
		return stockPriceChangeDAO.loadTop(tradingDate);
	}

	public List<StockPriceChange> loadBottom(Date tradingDate) throws StockException {
		return stockPriceChangeDAO.loadBottom(tradingDate);
	}

	public void updatePerformers(String dateString, int size) throws StockException {
		List<StockPriceChange> spcList = downloadAndSavePriceData(dateString);
		List<StockPriceChange> topList = spcList.stream().filter(spc -> spc.getChangePercent() > 0)
				.sorted(Comparator.comparing(StockPriceChange::getChangePercent).reversed()).limit(size)
				.collect(Collectors.toList());
		List<StockPriceChange> bottomList = spcList.stream().filter(spc -> spc.getChangePercent() < 0)
				.sorted(Comparator.comparing(StockPriceChange::getChangePercent)).limit(size)
				.collect(Collectors.toList());
		topList.stream().forEach(spc -> {
			try {
				spc.setStockItem(stockItemService.getOrCreate(spc.getSymbol()));
			} catch (StockException e) {
				throw new RuntimeException(e);
			}
		});
		stockPriceChangeDAO.saveTop(dateString, topList);
		//
		bottomList.stream().forEach(spc -> {
			try {
				spc.setStockItem(stockItemService.getOrCreate(spc.getSymbol()));
			} catch (StockException e) {
				throw new RuntimeException(e);
			}
		});
		stockPriceChangeDAO.saveBottom(dateString, bottomList);
	}

	public List<String> getStockSymbolsWithCallWarrant() {
		return callWarrantTradeSummaryDAO.getStockSymbolsWithCallWarrant();
	}

	public List<String> getStockSymbolsWithPutWarrant() {
		return putWarrantTradeSummaryDAO.getStockSymbolsWithPutWarrant();
	}

}

package com.javatican.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockPrice;
import com.javatican.stock.util.StockUtils;

/*
 * This service instance is used to perform actions on a specific stock 
 */
@Service("individualStockService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class IndividualStockService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// Url for downloading stock daily trading value/volume and prices(ohlc)
	private static final String TWSE_INDIVIDUAL_STOCK_DAILY_TRADING_GET_URL = "http://www.tse.com.tw/en/exchangeReport/STOCK_DAY?response=html&date=%s&stockNo=%s";
	private static final String TWSE_STOCK_PROFILE_GET_URL = "http://mops.twse.com.tw/mops/web/t05st03";
	private static final String TWSE_STOCK_PROFILE_POST_URL = "http://mops.twse.com.tw/mops/web/ajax_t05st03";
	@Autowired
	StockPriceDAO stockPriceDAO;
	@Autowired
	StockItemDAO stockItemDAO;

	public void createStockItem(String stockSymbol) throws StockException {
		Pattern cnameP = Pattern.compile("\\((\\S+)\\)\\s(\\S+)");
		Pattern capitalP = Pattern.compile("\\s*([\\d,-]+)元");
		try {
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
			reqParams.put("co_id", stockSymbol);
			Document doc = Jsoup.connect(TWSE_STOCK_PROFILE_POST_URL).cookie("jcsession", sessionId).data(reqParams)
					.post();
			StockItem si = new StockItem();
			si.setSymbol(stockSymbol);
			String compNameText = doc.selectFirst("table td.compName span").text();

			Matcher m = cnameP.matcher(compNameText);
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
			stockItemDAO.saveStockItem(si);
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

	/*
	 * Update stock daily trading value/volume and prices for a stock The data
	 * downloaded include records for the whole month, some may have been downloaded
	 * before. The duplicate ones will be checked in
	 * StockPriceDAO.addStockPriceList() method.
	 */
	public void updateData(String stockSymbol) throws StockException {
		String firstDayOfTheMonth = StockUtils.getFirstDayOfCurrentMonth();
		List<StockPrice> spList = initialStockPriceAndVolume(stockSymbol, firstDayOfTheMonth);
		stockPriceDAO.addStockPriceList(stockSymbol, spList);
	}

	/*
	 * Download daily trading value/volume and prices within past six months for
	 * stockSymbol, only need to run once.
	 */
	public void prepareData(String stockSymbol) throws StockException {
		// List<String> dateList = Arrays.asList("20180101", "20180201", "20180301",
		// "20180401", "20180501");
		List<String> dateList = StockUtils.calculateDateStringPastSixMonth();
		List<StockPrice> spList = new ArrayList<>();
		for (String dateString : dateList) {
			logger.info("prepare data for date:" + dateString);
			spList.addAll(initialStockPriceAndVolume(stockSymbol, dateString));
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
			}
		}
		stockPriceDAO.save(stockSymbol, spList);
	}

	/*
	 * This method parse the page at the url and return a list of StockPrice.
	 */
	private List<StockPrice> initialStockPriceAndVolume(String stockSymbol, String dateString) throws StockException {
		try {
			List<StockPrice> spList = new ArrayList<>();
			Document doc = Jsoup
					.connect(String.format(TWSE_INDIVIDUAL_STOCK_DAILY_TRADING_GET_URL, dateString, stockSymbol)).get();
			Elements trs = doc.select("table > tbody > tr");
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				StockPrice sp = new StockPrice();
				sp.setTradingDateAsString(tds.get(0).text());
				sp.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(1).text())));
				sp.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
				sp.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
				sp.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
				sp.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
				sp.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
				sp.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
				spList.add(sp);
			}
			return spList;
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

}

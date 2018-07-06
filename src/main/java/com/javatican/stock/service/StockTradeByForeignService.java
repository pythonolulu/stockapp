package com.javatican.stock.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.javatican.stock.dao.StockTradeByForeignDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

@Service("stockTradeByForeignService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockTradeByForeignService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_STOCK_TRADE_BY_FOREIGN_GET_URL = "http://www.tse.com.tw/fund/TWT38U?response=html&date=%s";
	private static String CALL_WARRANT_RE = "0[1-9][\\d]{4}";
	private static String PUT_WARRANT_RE = "0[\\d]{4}P";
	@Autowired
	StockConfig stockConfig;

	@Autowired
	StockTradeByForeignDAO stockTradeByForeignDAO;

	@Autowired
	TradingDateDAO tradingDateDAO;

	@Autowired
	StockItemDAO stockItemDAO;

	@Autowired
	StockItemService stockItemService;

	/*
	 * update stock trade data for Foreign (download and save the new available
	 * data)
	 */
	public void updateData() throws StockException {
		Date latest = stockTradeByForeignDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		downloadAndSave(tdList);
	}

	/*
	 * manual downloads history stock trade data by Foreign (only run once)
	 */
	public void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(StockUtils.stringToDate("2018/04/27").get(),
				StockUtils.stringToDate("2018/07/05").get());
		downloadAndSave(tdList);

	}

	/*
	 * download and save the stock trade data by Foreign for a list of TradingDate
	 * instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException {
		Map<String, StockItem> siMap = stockItemDAO.findAllAsMap();
		List<StockTradeByForeign> result = new ArrayList<>();

		for (TradingDate td : tdList) {
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			logger.info("prepare foreign data for date:" + dateString);
			//
			String strUrl = String.format(TWSE_STOCK_TRADE_BY_FOREIGN_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);

				Elements trs = doc.select("table > tbody > tr");
				StockTradeByForeign stbf;
				StockItem si = null;
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					String itemSymbol = tds.get(1).text();
					if (itemSymbol.matches(CALL_WARRANT_RE) || itemSymbol.matches(PUT_WARRANT_RE)) {
						logger.info("The symbol: " + itemSymbol + " is not a stock item and is skipped!");
						continue;
					}
					stbf = new StockTradeByForeign(td.getDate());
					stbf.setStockSymbol(itemSymbol);
					stbf.setBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					stbf.setSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					stbf.setDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					si = siMap.get(stbf.getStockSymbol());
					if (si == null) {
						si = stockItemService.downloadAndSaveStockProfile(stbf.getStockSymbol());
						siMap.put(stbf.getStockSymbol(), si);
					}
					stbf.setStockItem(si);
					result.add(stbf);
				}
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			} catch (Exception ex) {
				throw new StockException(ex);
			}

		}
		stockTradeByForeignDAO.saveAll(result);
	}
}

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
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

@Service("stockTradeByTrustService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class StockTradeByTrustService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_STOCK_TRADE_BY_TRUST_GET_URL = "https://www.twse.com.tw/fund/TWT44U?response=html&date=%s";

	@Autowired
	StockConfig stockConfig;

	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;

	@Autowired
	TradingDateDAO tradingDateDAO;

	@Autowired
	StockItemDAO stockItemDAO;

	@Autowired
	StockItemService stockItemService;

	/*
	 * update stock trade data for Trust (download and save the new available data)
	 */
	public void updateData() throws StockException {
		Date latest = stockTradeByTrustDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		downloadAndSave(tdList);
	}

	/*
	 * manual downloads history stock trade data by Trust (only run once)
	 */
	private void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(StockUtils.stringToDate("2018/05/01").get(),
				StockUtils.stringToDate("2018/05/27").get());
		downloadAndSave(tdList);

	}

	/*
	 * download and save the stock trade data by Trust for a list of TradingDate
	 * instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException {
		Map<String, StockItem> siMap = stockItemDAO.findAllAsMap();
		List<StockTradeByTrust> result = new ArrayList<>();
		for (TradingDate td : tdList) {
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			logger.info("prepare trust data for date:" + dateString);

			//
			String strUrl = String.format(TWSE_STOCK_TRADE_BY_TRUST_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
				//logger.info(doc.toString());
				Elements trs = doc.select("table > tbody > tr");
				StockTradeByTrust stbt;
				StockItem si = null;
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					stbt = new StockTradeByTrust(td.getDate());
					stbt.setStockSymbol(tds.get(1).text());
					//logger.info("prepare trust data for symbol:"+stbt.getStockSymbol());
					stbt.setBuy(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					stbt.setSell(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					stbt.setDiff(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					si = siMap.get(stbt.getStockSymbol());
					if (si == null) {
						si = stockItemService.downloadAndSaveStockProfile(stbt.getStockSymbol());
						siMap.put(stbt.getStockSymbol(), si);
					}
					stbt.setStockItem(si);
					result.add(stbt);
				}
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			} catch (Exception ex) {
				throw new StockException(ex);
			}
		}
		stockTradeByTrustDAO.saveAll(result);
	}
}

package com.javatican.stock.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.javatican.stock.dao.DealerTradeSummaryDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.model.WarrantTrade;
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.DealerTradeSummary;
import com.javatican.stock.util.StockUtils;

@Service("dealerTradeSummaryService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class DealerTradeSummaryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TWSE_DEALER_TRADE_GET_URL = "http://www.tse.com.tw/fund/TWT43U?response=html&date=%s";
	private String CALL_WARRANT_RE = "0[1-9][\\d]{4}";
	private String PUT_WARRANT_RE = "0[\\d]{4}P";

	@Autowired
	StockConfig stockConfig;

	@Autowired
	TradingDateDAO tradingDateDAO;

	@Autowired
	StockItemDAO stockItemDAO;

	@Autowired
	DealerTradeSummaryDAO dealerTradeSummaryDAO;

	@Autowired
	WarrantTradeDAO warrantTradeDAO;

	@Autowired
	StockItemService stockItemService;

	/*
	 * update dealer trade data (download and save the new available data)
	 */
	public void updateData() throws StockException {
		Date latest = dealerTradeSummaryDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		downloadAndSave(tdList);
	}

	/*
	 * manual downloads history warrant trade data (only run once)
	 */
	public void prepareData() throws StockException {
		List<TradingDate> tdList = tradingDateDAO.findBetween(StockUtils.stringToDate("2018/05/01").get(),
				StockUtils.stringToDate("2018/7/03").get());
		downloadAndSave(tdList);
	}

	/*
	 * download and save the warrant trade data for a list of TradingDate instances.
	 */
	private void downloadAndSave(List<TradingDate> tdList) throws StockException {
		Map<String, StockItem> siMap = stockItemDAO.findAllAsMap();
		Map<String, DealerTradeSummary> dtsMap;
		Map<String, WarrantTrade> cwtMap;
		Map<String, WarrantTrade> pwtMap;
		for (TradingDate td : tdList) {
			String dateString = StockUtils.dateToSimpleString(td.getDate());
			dtsMap = new TreeMap<>();
			cwtMap = warrantTradeDAO.loadCallWarrantsAsMap(dateString);
			pwtMap = warrantTradeDAO.loadPutWarrantsAsMap(dateString);
			logger.info("prepare dealder trade data for date:" + dateString);

			String strUrl = String.format(TWSE_DEALER_TRADE_GET_URL, dateString);
			try (InputStream inStream = new URL(strUrl).openStream();) {
				Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);

				Elements trs = doc.select("table > tbody > tr");
				DealerTradeSummary dts;
				StockItem si = null;
				String symbol;
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					// get the stock(or warrant) symbol
					String itemSymbol = tds.get(0).text();
					if(tds.get(5).text().equals("0") && tds.get(6).text().equals("0")) {
						logger.warn("The stock/warrant symbol: "+itemSymbol+" has no hedge trading data");
						continue;
					}
					// bugger! some symbol field is empty
					if (itemSymbol.equals("")) {
						logger.warn("The stock symbol is empty in the downloaded dealer trade data.");
						continue;
					} else if (itemSymbol.matches(CALL_WARRANT_RE)) {
						WarrantTrade wt = cwtMap.get(itemSymbol);
						if (wt == null) {
							logger.warn("The call warrant symbol: " + itemSymbol + " is not in the tracking list");
							continue;
						}
						symbol = wt.getStockSymbol();
						si = siMap.get(symbol);
						double avgPrice = wt.getAvgPrice();
						if ((dts = dtsMap.get(symbol)) == null) {
							dts = new DealerTradeSummary(td.getDate(), symbol);
							dtsMap.put(symbol, dts);
						}
						// accumulate various values
						dts.setHedgeCallBuy(dts.getHedgeCallBuy()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text()))));
						dts.setHedgeCallSell(dts.getHedgeCallSell()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text()))));
						dts.setHedgeCallNet(dts.getHedgeCallNet()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text()))));
					} else if (itemSymbol.matches(PUT_WARRANT_RE)) {
						WarrantTrade wt = pwtMap.get(itemSymbol);
						if (wt == null) {
							logger.warn("The put warrant symbol: " + itemSymbol + " is not in the tracking list");
							continue;
						}
						symbol = wt.getStockSymbol();
						si = siMap.get(symbol);
						double avgPrice = wt.getAvgPrice();
						if ((dts = dtsMap.get(symbol)) == null) {
							dts = new DealerTradeSummary(td.getDate(), symbol);
							dtsMap.put(symbol, dts);
						}
						// accumulate various values
						dts.setHedgePutBuy(dts.getHedgePutBuy()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text()))));
						dts.setHedgePutSell(dts.getHedgePutSell()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text()))));
						dts.setHedgePutNet(dts.getHedgePutNet()
								+ StockUtils.roundDoubleDp0(avgPrice * Integer.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text()))));
					} else {
						symbol = itemSymbol;
						if ((si = siMap.get(symbol)) == null) {
							logger.warn("Stock symbol: " + symbol + " in not listed in the current tracking list");
							continue;
						}
						double avgStockprice = si.getPrice();
						if ((dts = dtsMap.get(symbol)) == null) {
							dts = new DealerTradeSummary(td.getDate(), symbol);
							dtsMap.put(symbol, dts);
						}
						dts.setHedgeBuy(
								StockUtils.roundDoubleDp0(avgStockprice * Integer.parseInt(StockUtils.removeCommaInNumber(tds.get(5).text()))));
						dts.setHedgeSell(
								StockUtils.roundDoubleDp0(avgStockprice * Integer.parseInt(StockUtils.removeCommaInNumber(tds.get(6).text()))));
						dts.setHedgeNet(
								StockUtils.roundDoubleDp0(avgStockprice * Integer.parseInt(StockUtils.removeCommaInNumber(tds.get(7).text()))));
					}
					dts.setStockItem(si);
				}
				dealerTradeSummaryDAO.saveAll(dtsMap.values());
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

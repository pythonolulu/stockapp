package com.javatican.stock.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.javatican.stock.dao.FutureDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.FutureData;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

/* this service deals with downloading and saving trading date and total trading values
 * and trading values for three big investors
 */
@Service("futureService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class FutureService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String FUTURE_TRADING_POST_URL = "https://www.taifex.com.tw/cht/3/futDailyMarketReport";
	private static final String FUTURE_TRADING2_POST_URL = "https://www.taifex.com.tw/cht/3/futContractsDate";
	private static final String FUTURE_TRADING3_POST_URL = "https://www.taifex.com.tw/cht/3/largeTraderFutQry";
	@Autowired
	StockConfig stockConfig;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	FutureDataDAO futureDataDAO;

	public void updateData() throws StockException {
		Date latest = futureDataDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		List<Date> dList = new ArrayList<>();
		tdList.stream().forEach(item -> dList.add(item.getDate()));
		downloadAndSaveFutureData(dList);
	}

	// below only call once
	private void downloadAndSaveFutureData() throws StockException {
		List<Date> tradingDateList = tradingDateDAO.findAllTradingDate();
		downloadAndSaveFutureData(tradingDateList);
	}

	private void downloadAndSaveFutureData(List<Date> tdList) throws StockException {
		for (Date date : tdList) {
			String dateString = StockUtils.dateToStringSeparatedBySlash(date);
			//
			Map<String, String> reqParams = new HashMap<>();
			reqParams.put("queryType", "2");
			reqParams.put("marketCode", "0");
			reqParams.put("commodity_id", "TX");
			reqParams.put("queryDate", dateString);
			reqParams.put("MarketCode", "0");
			reqParams.put("commodity_idt", "TX");
			//
			Document doc;
			try {
				doc = Jsoup.connect(FUTURE_TRADING_POST_URL).data(reqParams).timeout(0).post();
				Elements tables = doc.select("table.table_f");
				if (tables == null || tables.isEmpty()) {
					logger.warn("no table_f in future html for date:" + date);
					throw new StockException("no table_f in future html for date:" + date);
				}
				//
				Elements trs = tables.get(0).select("tbody > tr");
				Element tr = trs.get(1);
				Elements tds = tr.select("td");
				FutureData fd = new FutureData(date);
				fd.setOpen(Double.valueOf(tds.get(2).text()));
				fd.setHigh(Double.valueOf(tds.get(3).text()));
				fd.setLow(Double.valueOf(tds.get(4).text()));
				fd.setClose(Double.valueOf(tds.get(5).text()));
				fd.setVolumeAfterHour(Double.valueOf(tds.get(8).text()));
				fd.setVolumeRegular(Double.valueOf(tds.get(9).text()));
				fd.setVolumeTotal(Double.valueOf(tds.get(10).text()));
				fd.setOpenInterest(Double.valueOf(tds.get(12).text()));
				//
				tr = trs.get(2);
				tds = tr.select("td");
				//
				fd.setOpen2(Double.valueOf(tds.get(2).text()));
				fd.setHigh2(Double.valueOf(tds.get(3).text()));
				fd.setLow2(Double.valueOf(tds.get(4).text()));
				fd.setClose2(Double.valueOf(tds.get(5).text()));
				fd.setVolumeAfterHour2(Double.valueOf(tds.get(8).text()));
				fd.setVolumeRegular2(Double.valueOf(tds.get(9).text()));
				fd.setVolumeTotal2(Double.valueOf(tds.get(10).text()));
				fd.setOpenInterest2(Double.valueOf(tds.get(12).text()));
				// download other fields
				downloadFutureData2(dateString, fd);
				downloadFutureData3(dateString, fd);
				futureDataDAO.save(fd);
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			} catch (Exception ex) {
				throw new StockException(ex);
			}
		}
	}

	//
	private void downloadFutureData2(String dateString, FutureData fd) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("queryType", "1");
		reqParams.put("doQuery", "1");
		reqParams.put("queryDate", dateString);
		reqParams.put("commodityId", "TXF");

		//
		Document doc;
		try {
			doc = Jsoup.connect(FUTURE_TRADING2_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in dealer/trust/foreign future html for date:" + dateString);
				throw new StockException("no trs in dealer/trust/foreign future html for date:" + dateString);
			}
			// 4th row : dealer
			Element tr = trs.get(3);
			Elements tds = tr.select("td");
			fd.setDealerTradingLong(parse1(tds.get(3)));
			fd.setDealerTradingShort(parse1(tds.get(5)));
			fd.setDealerTradingNet(parse1(tds.get(7)));
			fd.setDealerOpenLong(parse1(tds.get(9)));
			fd.setDealerOpenShort(parse1(tds.get(11)));
			fd.setDealerOpenNet(parse1(tds.get(13)));
			// 5th row: trust
			tr = trs.get(4);
			tds = tr.select("td");
			fd.setTrustTradingLong(parse1(tds.get(1)));
			fd.setTrustTradingShort(parse1(tds.get(3)));
			fd.setTrustTradingNet(parse1(tds.get(5)));
			fd.setTrustOpenLong(parse1(tds.get(7)));
			fd.setTrustOpenShort(parse1(tds.get(9)));
			fd.setTrustOpenNet(parse1(tds.get(11)));
			// 6th row: foreign
			tr = trs.get(5);
			tds = tr.select("td");
			fd.setForeignTradingLong(parse1(tds.get(1)));
			fd.setForeignTradingShort(parse1(tds.get(3)));
			fd.setForeignTradingNet(parse1(tds.get(5)));
			fd.setForeignOpenLong(parse1(tds.get(7)));
			fd.setForeignOpenShort(parse1(tds.get(9)));
			fd.setForeignOpenNet(parse1(tds.get(11)));
		} catch (Exception ex) {
			throw new StockException(ex);
		}

	}

	//
	private void downloadFutureData3(String dateString, FutureData fd) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("queryDate", dateString);
		reqParams.put("contractId", "TX");
		//
		Document doc;
		try {
			doc = Jsoup.connect(FUTURE_TRADING3_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in future html of top5/10 traders for date:" + dateString);
				throw new StockException("no trs in future html of top5/10 traders for date:" + dateString);
			}
			// 5th row : current month
			Element tr = trs.get(4);
			Elements tds = tr.select("td");
			fd.setBuyOiTop5(parse2(tds.get(1)));
			fd.setBuyRatioTop5(parse3(tds.get(2)));
			fd.setBuyOiTop10(parse2(tds.get(3)));
			fd.setBuyRatioTop10(parse3(tds.get(4)));
			fd.setSellOiTop5(parse2(tds.get(5)));
			fd.setSellRatioTop5(parse3(tds.get(6)));
			fd.setSellOiTop10(parse2(tds.get(7)));
			fd.setSellRatioTop10(parse3(tds.get(8)));
			fd.setTotalOi(parse4(tds.get(9)));
			// 6th row: all contract
			tr = trs.get(5);
			tds = tr.select("td");
			fd.setBuyOiTop5All(parse2(tds.get(1)));
			fd.setBuyRatioTop5All(parse3(tds.get(2)));
			fd.setBuyOiTop10All(parse2(tds.get(3)));
			fd.setBuyRatioTop10All(parse3(tds.get(4)));
			fd.setSellOiTop5All(parse2(tds.get(5)));
			fd.setSellRatioTop5All(parse3(tds.get(6)));
			fd.setSellOiTop10All(parse2(tds.get(7)));
			fd.setSellRatioTop10All(parse3(tds.get(8)));
			fd.setTotalOiAll(parse4(tds.get(9)));

		} catch (Exception ex) {
			throw new StockException(ex);
		}

	}

	private Double parse1(Element e) {
		String text = e.select("div > font").text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;

	}

	private Double parse2(Element e) {
		String text = e.select("div").text();
		String data = text.substring(0, text.indexOf("("));
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(data));
		return d;
	}

	private Double parse3(Element e) {
		String text = e.select("div").text();
		String data = text.substring(0, text.indexOf("%"));
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(data));
		return d;
	}

	private Double parse4(Element e) {
		String text = e.select("div").text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;
	}
}

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
@Service("futureServiceOld")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class FutureService2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String FUTURE_TRADING_POST_URL = "http://www.taifex.com.tw/chinese/3/3_1_1.asp";
	private static final String FUTURE_TRADING2_POST_URL = "http://www.taifex.com.tw/chinese/3/7_12_3.asp";
	private static final String FUTURE_TRADING3_POST_URL = "http://www.taifex.com.tw/chinese/3/7_8.asp";
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
			String dateString = StockUtils.dateToSimpleString(date);
			int[] yearMonthDay = StockUtils.getYearMonthDay(date);
			//
			Map<String, String> reqParams = new HashMap<>();
			reqParams.put("qtype", "2");
			reqParams.put("commodity_id", "TX");
			reqParams.put("market_code", "0");
			reqParams.put("dateaddcnt", "0");
			reqParams.put("DATA_DATE_Y", String.valueOf(yearMonthDay[0]));
			reqParams.put("DATA_DATE_M", String.valueOf(yearMonthDay[1]));
			reqParams.put("DATA_DATE_D", String.valueOf(yearMonthDay[2]));
			reqParams.put("syear", String.valueOf(yearMonthDay[0]));
			reqParams.put("smonth", String.valueOf(yearMonthDay[1]));
			reqParams.put("sday", String.valueOf(yearMonthDay[2]));
			reqParams.put("datestart", String.format("%s/%s/%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
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
				downloadFutureData2(yearMonthDay, fd);
				downloadFutureData3(yearMonthDay, fd);
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
	private void downloadFutureData2(int[] yearMonthDay, FutureData fd) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("DATA_DATE_Y", String.valueOf(yearMonthDay[0]));
		reqParams.put("DATA_DATE_M", String.valueOf(yearMonthDay[1]));
		reqParams.put("DATA_DATE_D", String.valueOf(yearMonthDay[2]));
		reqParams.put("syear", String.valueOf(yearMonthDay[0]));
		reqParams.put("smonth", String.valueOf(yearMonthDay[1]));
		reqParams.put("sday", String.valueOf(yearMonthDay[2]));
		reqParams.put("datestart", String.format("%s/%s/%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));

		//
		Document doc;
		try {
			doc = Jsoup.connect(FUTURE_TRADING2_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in dealer/trust/foreign future html for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
				throw new StockException("no trs in dealer/trust/foreign future html for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
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
	private void downloadFutureData3(int[] yearMonthDay, FutureData fd) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("yytemp", String.valueOf(yearMonthDay[0]));
		reqParams.put("mmtemp", String.valueOf(yearMonthDay[1]));
		reqParams.put("ddtemp", String.valueOf(yearMonthDay[2]));
		reqParams.put("chooseitemtemp", "ALL");
		reqParams.put("choose_yy", String.valueOf(yearMonthDay[0]));
		reqParams.put("choose_mm", String.valueOf(yearMonthDay[1]));
		reqParams.put("choose_dd", String.valueOf(yearMonthDay[2]));
		reqParams.put("datestart", String.format("%s/%s/%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
		reqParams.put("choose_item", "ALL");
		//
		Document doc;
		try {
			doc = Jsoup.connect(FUTURE_TRADING3_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in future html of top5/10 traders for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
				throw new StockException("no trs in future html of top5/10 traders for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
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

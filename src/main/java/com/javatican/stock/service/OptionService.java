package com.javatican.stock.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
import com.javatican.stock.dao.OptionDataDAO;
import com.javatican.stock.dao.OptionSeriesDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.model.OptionData;
import com.javatican.stock.model.OptionSeriesData;
import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

/* this service deals with downloading and saving trading date and total trading values
 * and trading values for three big investors
 */
@Service("optionService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class OptionService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String OPTION_TRADING_POST_URL = "http://www.taifex.com.tw/chinese/3/3_2_2.asp";
	private static final String OPTION_TRADING2_POST_URL = "http://www.taifex.com.tw/chinese/3/7_12_5.asp";
	private static final String OPTION_TRADING3_POST_URL = "http://www.taifex.com.tw/chinese/3/7_9.asp";
	@Autowired
	StockConfig stockConfig;
	@Autowired
	TradingDateDAO tradingDateDAO;
	@Autowired
	OptionDataDAO optionDataDAO;
	@Autowired
	OptionSeriesDataDAO optionSeriesDataDAO;

	public void updateData() throws StockException {
		Date latest = optionDataDAO.getLatestTradingDate();
		List<TradingDate> tdList = tradingDateDAO.findAfter(latest);
		List<Date> dList = new ArrayList<>();
		tdList.stream().forEach(item -> dList.add(item.getDate()));
		downloadAndSaveOptionData(dList);
	}

	// below only call once
	public void downloadAndSaveOptionData() throws StockException {
		// List<Date> tradingDateList = tradingDateDAO.findAllTradingDate();
		List<Date> tradingDateList = tradingDateDAO.findDateByDateBetween(
				StockUtils.stringSimpleToDate("20180401").get(), StockUtils.stringSimpleToDate("20180831").get());
		downloadAndSaveOptionData(tradingDateList);
	}

	private void downloadAndSaveOptionData(List<Date> tdList) throws StockException {
		for (Date date : tdList) {
			// String dateString = StockUtils.dateToSimpleString(date);
			int[] yearMonthDay = StockUtils.getYearMonthDay(date);
			//
			Map<String, String> reqParams = new HashMap<>();
			reqParams.put("qtype", "2");
			reqParams.put("commodity_id", "TXO");
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
			reqParams.put("commodity_idt", "TXO");
			//
			Document doc;
			try {
				doc = Jsoup.connect(OPTION_TRADING_POST_URL).data(reqParams).timeout(0).post();
				Elements trs = doc.select("table.table_c > tbody > tr");
				if (trs == null || trs.isEmpty()) {
					logger.warn("no table_c tr element in option html for date:" + date);
					throw new StockException("no table_c tr element in option html for date:" + date);
				}
				String[] contractNames = this.getContractName(trs);
				String weekContract = contractNames[0]; // maybe null
				String currentMonthContract = contractNames[1];
				String nextMonthContract = contractNames[2];
				//
				List<OptionSeriesData> osdList = new ArrayList<>();
				//
				double callTradingVolume = 0.0;
				double putTradingVolume = 0.0;
				double callOi = 0.0;
				double putOi = 0.0;
				boolean isCallOption = false;
				//
				for (int i = 1; i < trs.size() - 2; i++) {
					Element tr = trs.get(i);
					Elements tds = tr.select("td");
					isCallOption = parseCallOption(tds.get(3));
					// accumulate volume and OI value
					if (isCallOption) {
						callTradingVolume += parseSimple1(tds.get(13));
						callOi += parseSimple1(tds.get(14));
					} else {
						putTradingVolume += parseSimple1(tds.get(13));
						putOi += parseSimple1(tds.get(14));
					}
					if (!toSaveItem(weekContract, currentMonthContract, nextMonthContract, parseSimple3(tds.get(1)),
							parseSimple1(tds.get(12)))) {
						continue;
					}
					int strikePrice = parseSimple2(tds.get(2));
					String contractName = parseSimple3(tds.get(1));
					//
					OptionSeriesData osd = new OptionSeriesData(date, strikePrice, isCallOption,
							contractName.equals(weekContract), currentMonthContract.equals(contractName),
							nextMonthContract.equals(contractName));
					osdList.add(osd);
					osd.setOpen(parseSimple1(tds.get(4)));
					osd.setHigh(parseSimple1(tds.get(5)));
					osd.setLow(parseSimple1(tds.get(6)));
					osd.setClose(parseSimple1(tds.get(7)));
					osd.setVolumeAfterHour(parseSimple1(tds.get(11)));
					osd.setVolumeRegular(parseSimple1(tds.get(12)));
					osd.setVolumeTotal(parseSimple1(tds.get(13)));
					osd.setOpenInterest(parseSimple1(tds.get(14)));
				}
				optionSeriesDataDAO.saveAll(osdList);
				//
				OptionData od = new OptionData(date);
				od.setCallTradingVolume(callTradingVolume);
				od.setPutTradingVolume(putTradingVolume);
				od.setCallOi(callOi);
				od.setPutOi(putOi);
				// download other fields
				downloadOptionData2(yearMonthDay, od);
				downloadOptionData3(yearMonthDay, od);
				optionDataDAO.save(od);
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
		}
	}

	private void downloadOptionData2(int[] yearMonthDay, OptionData od) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("DATA_DATE_Y", String.valueOf(yearMonthDay[0]));
		reqParams.put("DATA_DATE_M", String.valueOf(yearMonthDay[1]));
		reqParams.put("DATA_DATE_D", String.valueOf(yearMonthDay[2]));
		reqParams.put("syear", String.valueOf(yearMonthDay[0]));
		reqParams.put("smonth", String.valueOf(yearMonthDay[1]));
		reqParams.put("sday", String.valueOf(yearMonthDay[2]));
		reqParams.put("datestart", String.format("%s/%s/%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
		reqParams.put("COMMODITY_ID", "TXO");
		//
		Document doc;
		try {
			doc = Jsoup.connect(OPTION_TRADING2_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in dealer/trust/foreign option html for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
				throw new StockException("no trs in dealer/trust/foreign option html for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
			}
			// 4th row : call option dealer
			Element tr = trs.get(3);
			Elements tds = tr.select("td");
			od.setCallDealerTradingLong(parse1(tds.get(4)));
			od.setCallDealerTradingValueLong(parseLousyTd(tds.get(5)));
			od.setCallDealerTradingShort(parse1(tds.get(6)));
			od.setCallDealerTradingValueShort(parse2(tds.get(7)));
			od.setCallDealerTradingNet(parse1(tds.get(8)));
			od.setCallDealerTradingValueNet(parse2(tds.get(9)));
			od.setCallDealerOpenLong(parse1(tds.get(10)));
			od.setCallDealerOpenValueLong(parse2(tds.get(11)));
			od.setCallDealerOpenShort(parse1(tds.get(12)));
			od.setCallDealerOpenValueShort(parse2(tds.get(13)));
			od.setCallDealerOpenNet(parse1(tds.get(14)));
			od.setCallDealerOpenValueNet(parse2(tds.get(15)));
			// 5th row: call option trust
			tr = trs.get(4);
			tds = tr.select("td");
			od.setCallTrustTradingLong(parse1(tds.get(1)));
			od.setCallTrustTradingValueLong(parseLousyTd(tds.get(2)));
			od.setCallTrustTradingShort(parse1(tds.get(3)));
			od.setCallTrustTradingValueShort(parse2(tds.get(4)));
			od.setCallTrustTradingNet(parse1(tds.get(5)));
			od.setCallTrustTradingValueNet(parse2(tds.get(6)));
			od.setCallTrustOpenLong(parse1(tds.get(7)));
			od.setCallTrustOpenValueLong(parse2(tds.get(8)));
			od.setCallTrustOpenShort(parse1(tds.get(9)));
			od.setCallTrustOpenValueShort(parse2(tds.get(10)));
			od.setCallTrustOpenNet(parse1(tds.get(11)));
			od.setCallTrustOpenValueNet(parse2(tds.get(12)));
			// 6th row: call option foreign
			tr = trs.get(5);
			tds = tr.select("td");
			od.setCallForeignTradingLong(parse1(tds.get(1)));
			od.setCallForeignTradingValueLong(parseLousyTd(tds.get(2)));
			od.setCallForeignTradingShort(parse1(tds.get(3)));
			od.setCallForeignTradingValueShort(parse2(tds.get(4)));
			od.setCallForeignTradingNet(parse1(tds.get(5)));
			od.setCallForeignTradingValueNet(parse2(tds.get(6)));
			od.setCallForeignOpenLong(parse1(tds.get(7)));
			od.setCallForeignOpenValueLong(parse2(tds.get(8)));
			od.setCallForeignOpenShort(parse1(tds.get(9)));
			od.setCallForeignOpenValueShort(parse2(tds.get(10)));
			od.setCallForeignOpenNet(parse1(tds.get(11)));
			od.setCallForeignOpenValueNet(parse2(tds.get(12)));
			// 7th row : put option dealer
			tr = trs.get(6);
			tds = tr.select("td");
			od.setPutDealerTradingLong(parse1(tds.get(2)));
			od.setPutDealerTradingValueLong(parseLousyTd(tds.get(3)));
			od.setPutDealerTradingShort(parse1(tds.get(4)));
			od.setPutDealerTradingValueShort(parse2(tds.get(5)));
			od.setPutDealerTradingNet(parse1(tds.get(6)));
			od.setPutDealerTradingValueNet(parse2(tds.get(7)));
			od.setPutDealerOpenLong(parse1(tds.get(8)));
			od.setPutDealerOpenValueLong(parse2(tds.get(9)));
			od.setPutDealerOpenShort(parse1(tds.get(10)));
			od.setPutDealerOpenValueShort(parse2(tds.get(11)));
			od.setPutDealerOpenNet(parse1(tds.get(12)));
			od.setPutDealerOpenValueNet(parse2(tds.get(13)));
			// 8th row: put option trust
			tr = trs.get(7);
			tds = tr.select("td");
			od.setPutTrustTradingLong(parse1(tds.get(1)));
			od.setPutTrustTradingValueLong(parseLousyTd(tds.get(2)));
			od.setPutTrustTradingShort(parse1(tds.get(3)));
			od.setPutTrustTradingValueShort(parse2(tds.get(4)));
			od.setPutTrustTradingNet(parse1(tds.get(5)));
			od.setPutTrustTradingValueNet(parse2(tds.get(6)));
			od.setPutTrustOpenLong(parse1(tds.get(7)));
			od.setPutTrustOpenValueLong(parse2(tds.get(8)));
			od.setPutTrustOpenShort(parse1(tds.get(9)));
			od.setPutTrustOpenValueShort(parse2(tds.get(10)));
			od.setPutTrustOpenNet(parse1(tds.get(11)));
			od.setPutTrustOpenValueNet(parse2(tds.get(12)));
			// 9th row: put option foreign
			tr = trs.get(8);
			tds = tr.select("td");
			od.setPutForeignTradingLong(parse1(tds.get(1)));
			od.setPutForeignTradingValueLong(parseLousyTd(tds.get(2)));
			od.setPutForeignTradingShort(parse1(tds.get(3)));
			od.setPutForeignTradingValueShort(parse2(tds.get(4)));
			od.setPutForeignTradingNet(parse1(tds.get(5)));
			od.setPutForeignTradingValueNet(parse2(tds.get(6)));
			od.setPutForeignOpenLong(parse1(tds.get(7)));
			od.setPutForeignOpenValueLong(parse2(tds.get(8)));
			od.setPutForeignOpenShort(parse1(tds.get(9)));
			od.setPutForeignOpenValueShort(parse2(tds.get(10)));
			od.setPutForeignOpenNet(parse1(tds.get(11)));
			od.setPutForeignOpenValueNet(parse2(tds.get(12)));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new StockException(ex);
		}
	}

	//
	private void downloadOptionData3(int[] yearMonthDay, OptionData od) throws StockException {
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("yytemp", String.valueOf(yearMonthDay[0]));
		reqParams.put("mmtemp", String.valueOf(yearMonthDay[1]));
		reqParams.put("ddtemp", String.valueOf(yearMonthDay[2]));
		reqParams.put("chooseitemtemp", "ALL");
		reqParams.put("choose_yy", String.valueOf(yearMonthDay[0]));
		reqParams.put("choose_mm", String.valueOf(yearMonthDay[1]));
		reqParams.put("choose_dd", String.valueOf(yearMonthDay[2]));
		reqParams.put("datestart", String.format("%s/%s/%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
		reqParams.put("choose_item", "TXO");
		//
		Document doc;
		try {
			doc = Jsoup.connect(OPTION_TRADING3_POST_URL).data(reqParams).timeout(0).post();
			Elements trs = doc.select("table.table_f > tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no trs in option html of top5/10 traders for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
				throw new StockException("no trs in option html of top5/10 traders for date:"
						+ String.format("%s%s%s", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
			}
			// current week data may be empty(the trading days within the 3rd week)
			// 4th row : call option, current week
			Element tr = trs.get(3);
			Elements tds = tr.select("td");
			if (!parseSimple3(tds.get(1)).contains("-")) {
				od.setCallBuyOiTop5Week(parse3(tds.get(2)));
				od.setCallBuyOiTop10Week(parse3(tds.get(4)));
				od.setCallSellOiTop5Week(parse3(tds.get(6)));
				od.setCallSellOiTop10Week(parse3(tds.get(8)));
				od.setCallTotalOiWeek(parse4(tds.get(10)));
			}
			// 5th row : call option, current month
			tr = trs.get(4);
			tds = tr.select("td");
			od.setCallBuyOiTop5(parse3(tds.get(1)));
			od.setCallBuyOiTop10(parse3(tds.get(3)));
			od.setCallSellOiTop5(parse3(tds.get(5)));
			od.setCallSellOiTop10(parse3(tds.get(7)));
			od.setCallTotalOi(parse4(tds.get(9)));
			// 6th row: call option, all contract
			tr = trs.get(5);
			tds = tr.select("td");
			od.setCallBuyOiTop5All(parse3(tds.get(1)));
			od.setCallBuyOiTop10All(parse3(tds.get(3)));
			od.setCallSellOiTop5All(parse3(tds.get(5)));
			od.setCallSellOiTop10All(parse3(tds.get(7)));
			od.setCallTotalOiAll(parse4(tds.get(9)));
			// current week data may be empty(the trading days within the 3rd week)
			// 7th row : put option, current week
			tr = trs.get(6);
			tds = tr.select("td");
			if (!parseSimple3(tds.get(1)).contains("-")) {
				od.setPutBuyOiTop5Week(parse3(tds.get(2)));
				od.setPutBuyOiTop10Week(parse3(tds.get(4)));
				od.setPutSellOiTop5Week(parse3(tds.get(6)));
				od.setPutSellOiTop10Week(parse3(tds.get(8)));
				od.setPutTotalOiWeek(parse4(tds.get(10)));
			}
			// 8th row : put option, current month
			tr = trs.get(7);
			tds = tr.select("td");
			od.setPutBuyOiTop5(parse3(tds.get(1)));
			od.setPutBuyOiTop10(parse3(tds.get(3)));
			od.setPutSellOiTop5(parse3(tds.get(5)));
			od.setPutSellOiTop10(parse3(tds.get(7)));
			od.setPutTotalOi(parse4(tds.get(9)));
			// 9th row: put option, all contract
			tr = trs.get(8);
			tds = tr.select("td");
			od.setPutBuyOiTop5All(parse3(tds.get(1)));
			od.setPutBuyOiTop10All(parse3(tds.get(3)));
			od.setPutSellOiTop5All(parse3(tds.get(5)));
			od.setPutSellOiTop10All(parse3(tds.get(7)));
			od.setPutTotalOiAll(parse4(tds.get(9)));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new StockException(ex);
		}

	}

	private Double parse1(Element e) {
		String text = e.select("font").text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;

	}

	private Double parse2(Element e) {
		String text = e.text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;

	}

	/*
	 * some td elements contains two div elements and data is in 2nd div element
	 */
	private Double parseLousyTd(Element e) {
		Elements divs = e.select("div");
		String text = divs.get(divs.size() - 1).text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;
	}

	private Double parse3(Element e) {
		String text = e.select("div").text();
		String data = text.substring(0, text.indexOf("("));
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(data));
		return d;
	}

	private Double parse4(Element e) {
		String text = e.select("div").text();
		Double d = Double.valueOf(StockUtils.removeCommaInNumber(text));
		return d;
	}

	// private Double parse5(Element e) {
	// String text = e.select("div").text();
	// String data = text.substring(0, text.indexOf("%"));
	// Double d = Double.valueOf(StockUtils.removeCommaInNumber(data));
	// return d;
	// }
	private Double parseSimple1(Element e) {
		return Double.valueOf(e.text());
	}

	private Integer parseSimple2(Element e) {
		return Integer.valueOf(e.text());
	}

	private String parseSimple3(Element e) {
		return e.text();
	}

	private boolean parseCallOption(Element e) {
		return e.text().contains("Call");
	}

	private String[] getContractName(Elements trs) {
		TreeSet<String> uniqueContractNames = new TreeSet<>();
		for (int i = 1; i < trs.size() - 2; i++) {
			Element tr = trs.get(i);
			Elements tds = tr.select("td");
			uniqueContractNames.add(tds.get(1).text());
		}
		String weekContract = null; // note that weekContract can be null, for example, when the trading days are
									// within the 3rd week period.
		String currentMonthContract = null;
		String nextMonthContract = null;
		for (String contract : uniqueContractNames) {
			if (contract.contains("W")) {
				// if there are two weekly option data available(ie. on Wednesday), the second
				// one will be return, for example, if both W4 and W5 are available, it will
				// return W5.
				weekContract = contract;
			} else if (currentMonthContract == null) {
				currentMonthContract = contract;
			} else if (nextMonthContract == null) {
				nextMonthContract = contract;
			} else {
				break;
			}
		}
		return new String[] { weekContract, currentMonthContract, nextMonthContract };
	}

	// weekContract maybe null
	private boolean toSaveItem(String weekContract, String currentMonthContract, String nextMonthContract,
			String contractMonthOrWeek, Double volumeRegular) {
		if (contractMonthOrWeek.equals(weekContract) || currentMonthContract.equals(contractMonthOrWeek)
				|| nextMonthContract.equals(contractMonthOrWeek)) {
			if (volumeRegular == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}

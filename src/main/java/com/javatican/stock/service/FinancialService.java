package com.javatican.stock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.javatican.stock.dao.FinancialInfoDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.model.FinancialInfo;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockUtils;

@Service("financialService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class FinancialService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String FINANCIAL_REPORT_POST_URL = "http://mops.twse.com.tw/server-java/t164sb01";

	@Autowired
	StockConfig stockConfig;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	FinancialInfoDAO financialInfoDAO;

	/*
	 * run every season, check twse official publish dates (change year/season
	 * accordingly). Parameter checkExists: normally set to false, but sometimes the
	 * same year/season data need to be re-download, then set this to true to avoid
	 * duplicate error
	 */
	public void updateData(boolean checkExists) {
		List<StockItem> siList = stockItemDAO.findAll();
		int year = 2017;
		int season = 2;
		downloadAndSave(siList, year, season, checkExists, true);
	}

	/*
	 * download all data for symbol
	 */
	public void updateData(String symbol) throws StockException {
		StockItem si = stockItemDAO.findBySymbol(symbol);
		downloadAndSaveAllFor(si);
	}

	private void downloadAndSaveAllFor(StockItem si) throws StockException {
		List<FinancialInfo> fiList = new ArrayList<>();
		List<Integer> years = Arrays.asList(2013, 2014, 2015, 2016, 2017);
		List<Integer> seasons = Arrays.asList(1, 2, 3, 4);

		for (Integer year : years) {
			for (Integer season : seasons) {
				FinancialInfo fi = downloadCombined(null, si, year, season);
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
				if (fi == null)
					continue;
				fiList.add(fi);
			}
		}
		// for the latest season 2018-1
		FinancialInfo fi = downloadCombined(null, si, 2018, 1);
		if (fi == null)
			fiList.add(fi);
		financialInfoDAO.saveAll(fiList);
	}

	/*
	 * download and save data for symbol for year/season
	 */
	private void downloadAndSave(StockItem si, int year, int season) throws StockException {
		FinancialInfo fi = financialInfoDAO.getByYearAndSeasonAndSymbol(year, season, si.getSymbol());
		if (fi == null) {
			fi = new FinancialInfo(si.getSymbol(), StockUtils.calculateAuditDate(year, season), year, season);
			fi.setStockItem(si);
		}
		downloadCombined(fi, si, year, season);
		financialInfoDAO.save(fi);
	}

	/*
	 * download and save data for stock items in the list. Parameters: checkExists:
	 * set to true to avoid duplicate error, retry: set to true if previous failed
	 * downloads need to be retried.
	 */
	private void downloadAndSave(List<StockItem> siList, int year, int season, boolean checkExists, boolean retry) {
		List<FinancialInfo> fiList = new ArrayList<>();
		// load ignore list of symbols.
		List<String> ignoreSymbolList = financialInfoDAO.loadIgnoreList();
		List<StockItem> retrySymbolList = new ArrayList<>();
		for (StockItem si : siList) {
			// exclude those ETFs
			if (si.getSymbol().length() > 4 || si.getSymbol().startsWith("0"))
				continue;
			if (ignoreSymbolList != null && ignoreSymbolList.contains(si.getSymbol()))
				continue;
			if (checkExists && financialInfoDAO.existsByYearAndSeasonAndSymbol(year, season, si.getSymbol())) {
				continue;
			}
			FinancialInfo fi = null;
			try {
				fi = downloadCombined(null, si, year, season);
			} catch (StockException e) {
				if (retry) {
					retrySymbolList.add(si);
				}
			}
			try {
				Thread.sleep(stockConfig.getSleepTime());
			} catch (InterruptedException ex) {
			}
			if (fi == null) {
				continue;
			}
			fiList.add(fi);
		}
		financialInfoDAO.saveAll(fiList);
		if (retry && !retrySymbolList.isEmpty()) {
			downloadAndSave(retrySymbolList, year, season, false, false);
		}

	}

	/*
	 * if FinancialInfo parameter is not null, it will download the data and update
	 * the instance. if the parameter is null, it will create a new instance. The
	 * method will try to download the financial report using 'C' type of reportId,
	 * which is of combined financial report
	 */
	private FinancialInfo downloadCombined(FinancialInfo fi, StockItem si, Integer year, Integer season)
			throws StockException {
		logger.info("update combined financial info data for symbol:" + si.getSymbol());
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("step", "1");
		reqParams.put("DEBUG", "");
		reqParams.put("CO_ID", si.getSymbol());
		reqParams.put("SYEAR", year.toString());
		reqParams.put("SSEASON", season.toString());
		reqParams.put("REPORT_ID", "C");
		//
		Document doc;
		try {
			doc = Jsoup.connect(FINANCIAL_REPORT_POST_URL).data(reqParams).timeout(0).post();
			// Balance sheet
			Elements tables = doc.select("table.result_table");
			if (tables == null || tables.isEmpty()) {
				logger.warn("no result_table in html... trying downloading individual report...");
				return downloadIndividual(fi, si, year, season);
			}
			//
			Elements trs = tables.get(0).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in result_table");
				return null;
			}
			// if fi is null, create a new instance.
			if (fi == null) {
				fi = new FinancialInfo(si.getSymbol(), StockUtils.calculateAuditDate(year, season), year, season);
				fi.setStockItem(si);
			}
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of result_table");
					continue;
				}
				checkAndRetrieveForBalanceSheet(tds, fi);
			}
			// Incoming statement and Statement of Cash Flow
			tables = doc.select("table.main_table");
			if (tables == null || tables.isEmpty()) {
				logger.warn("no main_table in html");
				return null;
			}
			// Incoming statement
			trs = tables.get(0).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in in main_table 0");
				return null;
			}
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of main_table 0");
					continue;
				}
				checkAndRetrieveForIncomeStatement(tds, fi);
			}
			// Statement of Cash Flow
			trs = tables.get(1).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in in main_table 1");
				return null;
			}
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of main_table 1");
					continue;
				}
				checkAndRetrieveForCashFlowStatement(tds, fi);
			}
			// calculate other fields.
			calculateFields(fi);
			return fi;
		} catch (IOException e) {
			e.printStackTrace();
			throw new StockException(e);
		}
	}

	/*
	 * download the financial info data using 'individual' report (reportID='A').
	 */
	private FinancialInfo downloadIndividual(FinancialInfo fi, StockItem si, Integer year, Integer season)
			throws StockException {
		logger.info("download individual financial info data for symbol:" + si.getSymbol());
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("step", "1");
		reqParams.put("DEBUG", "");
		reqParams.put("CO_ID", si.getSymbol());
		reqParams.put("SYEAR", year.toString());
		reqParams.put("SSEASON", season.toString());
		reqParams.put("REPORT_ID", "A");
		//
		Document doc;
		try {
			doc = Jsoup.connect(FINANCIAL_REPORT_POST_URL).data(reqParams).timeout(0).post();
			// Balance sheet
			Elements tables = doc.select("table.result_table");
			if (tables == null || tables.isEmpty()) {
				logger.warn("no result_table in html");
				return null;
			}
			//
			Elements trs = tables.get(0).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in result_table");
				return null;
			}
			if (fi == null) {
				fi = new FinancialInfo(si.getSymbol(), StockUtils.calculateAuditDate(year, season), year, season);
				fi.setStockItem(si);
			}

			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of result_table");
					continue;
				}
				checkAndRetrieveForBalanceSheet(tds, fi);
			}
			// Incoming statement and Statement of Cash Flow
			tables = doc.select("table.main_table");
			if (tables == null || tables.isEmpty()) {
				logger.warn("no main_table in html");
				return null;
			}
			// Incoming statement
			trs = tables.get(0).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in in main_table 0");
				return null;
			}
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of main_table 0");
					continue;
				}
				checkAndRetrieveForIncomeStatement(tds, fi);
			}
			// Statement of Cash Flow
			trs = tables.get(1).select("tbody > tr");
			if (trs == null || trs.isEmpty()) {
				logger.warn("no tr in in main_table 1");
				return null;
			}
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				if (tds == null || tds.isEmpty()) {
					// logger.warn("no td in tr of main_table 1");
					continue;
				}
				checkAndRetrieveForCashFlowStatement(tds, fi);
			}
			// calculate other fields.
			calculateFields(fi);
			return fi;
		} catch (IOException e) {
			e.printStackTrace();
			throw new StockException(e);
		}
	}

	private void checkAndRetrieveForCashFlowStatement(Elements tds, FinancialInfo fi) {
		String label = tds.get(0).text().replace('\u3000', '\u0020').trim();
		String value = tds.get(1).text().replace('\u3000', '\u0020').trim();
		if (value == null || value.equals(""))
			return;
		double valueD = Double.valueOf(StockUtils.removeCommaInNumber(value));
		if (label.equals("營業活動之淨現金流入（流出）")) {
			fi.setNetCashOperating(valueD);
		} else if (label.equals("投資活動之淨現金流入（流出）")) {
			fi.setNetCashInvesting(valueD);
		} else if (label.equals("籌資活動之淨現金流入（流出）")) {
			fi.setNetCashFinancing(valueD);
		} else if (label.equals("匯率變動對現金及約當現金之影響")) {
			fi.setEffectExchangeRate(valueD);
		} else if (label.equals("本期現金及約當現金增加（減少）數")) {
			fi.setNetCash(valueD);
		} else if (label.equals("期初現金及約當現金餘額")) {
			fi.setCashBegin(valueD);
		} else if (label.equals("期末現金及約當現金餘額")) {
			fi.setCashEnd(valueD);
		}
	}

	private void checkAndRetrieveForIncomeStatement(Elements tds, FinancialInfo fi) {
		String label = tds.get(0).text().replace('\u3000', '\u0020').trim();
		String value = tds.get(1).text().replace('\u3000', '\u0020').trim();
		if (value == null || value.equals(""))
			return;
		double valueD = Double.valueOf(StockUtils.removeCommaInNumber(value));
		if (label.equals("營業收入合計")) {
			fi.setNetRevenue(valueD);
		} else if (label.equals("營業成本合計")) {
			fi.setCostRevenue(valueD);
		} else if (label.equals("營業毛利（毛損）淨額")) {
			fi.setGrossProfit(valueD);
		} else if (label.equals("營業費用合計")) {
			fi.setTotalOperatingExpenses(valueD);
		} else if (label.equals("營業利益（損失）")) {
			fi.setIncomeFromOperations(valueD);
		} else if (label.equals("營業外收入及支出合計")) {
			fi.setNonOperatingIncomeExpenses(valueD);
		} else if (label.equals("繼續營業單位稅前淨利（淨損）")) {
			fi.setIncomeBeforeTax(valueD);
		} else if (label.equals("所得稅費用（利益）合計")) {
			fi.setIncomeTax(valueD);
		} else if (label.equals("本期淨利（淨損）") || label.equals("本期稅前淨利（淨損）")) {
			fi.setNetIncome(valueD);
		} else if (label.equals("基本每股盈餘合計")) {
			fi.setEps(valueD);
		}
	}

	private void calculateFields(FinancialInfo fi) {
		double capitalStockParValue = fi.getCapitalStockParValue();
		double equityShareholdersParent = fi.getEquityShareholdersParent();
		if (capitalStockParValue != 0) {
			fi.setNetAssetValuePerShare(
					StockUtils.roundDoubleDp2(10.0 * equityShareholdersParent / capitalStockParValue));
		} else {
			logger.warn("Can not calculate netAssetValuePerShare, because capitalStockParValue is 0");
		}
		//
		double netRevenue = fi.getNetRevenue();
		double grossProfit = fi.getGrossProfit();
		if (netRevenue != 0) {
			fi.setProfitMargin(StockUtils.roundDoubleDp4(grossProfit / netRevenue));
		} else {
			logger.warn("Can not calculate profitMargin, because netRevenue is 0");
		}
		//
		double netCashOperating = fi.getNetCashOperating();
		double netCashInvesting = fi.getNetCashInvesting();
		fi.setFreeCashFlow(StockUtils.roundDoubleDp4(netCashOperating + netCashInvesting));
		if (fi.getLiabilitiesAndEquity() == 0.0) {
			fi.setLiabilitiesAndEquity(fi.getAssetsTotal());
		}
	}

	private void checkAndRetrieveForBalanceSheet(Elements tds, FinancialInfo fi) {
		String label = tds.get(0).text().replace('\u3000', '\u0020').trim();
		String value = tds.get(1).text().replace('\u3000', '\u0020').trim();
		// logger.info(String.format("label=%s, value=%s", label, value));
		if (value == null || value.equals(""))
			return;
		double valueD = Double.valueOf(StockUtils.removeCommaInNumber(value));
		if (label.equals("現金及約當現金合計") || label.equals("現金及約當現金") || label.equals("現金及約當現金總額")) {
			fi.setAssetsCash(valueD);
		} else if (label.equals("應收帳款淨額")) {
			fi.setAssetsAccountReceivable(fi.getAssetsAccountReceivable() + valueD);
		} else if (label.equals("應收帳款－關係人淨額")) {
			fi.setAssetsAccountReceivable(fi.getAssetsAccountReceivable() + valueD);
		} else if (label.equals("其他應收款－關係人淨額")) {
			fi.setAssetsAccountReceivable(fi.getAssetsAccountReceivable() + valueD);
		} else if (label.equals("存貨合計") || label.equals("存貨")) {
			fi.setAssetsInventories(valueD);
		} else if (label.equals("流動資產合計")) {
			fi.setAssetsCurrent(valueD);
		} else if (label.equals("不動產、廠房及設備合計") || label.equals("不動產、廠房及設備")) {
			fi.setAssetsPpe(valueD);
		} else if (label.equals("非流動資產合計")) {
			fi.setAssetsNonCurrent(valueD);
		} else if (label.equals("資產總計") || label.equals("資產總額")) {
			fi.setAssetsTotal(valueD);
		} else if (label.equals("流動負債合計")) {
			fi.setLiabilitiesCurrent(valueD);
		} else if (label.equals("非流動負債合計")) {
			fi.setLiabilitiesNonCurrent(valueD);
		} else if (label.equals("負債總計") || label.equals("負債總額")) {
			fi.setLiabilitiesTotal(valueD);
		} else if (label.equals("股本合計")) {
			fi.setCapitalStockParValue(valueD);
		} else if (label.equals("歸屬於母公司業主之權益合計") || label.equals("權益總計") || label.equals("權益總額")) {
			fi.setEquityShareholdersParent(valueD);
		} else if (label.equals("負債及權益總計")) {
			fi.setLiabilitiesAndEquity(valueD);
		}
	}

}
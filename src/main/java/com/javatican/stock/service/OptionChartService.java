package com.javatican.stock.service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.future.chart.FuturePricePlot;
import com.javatican.stock.option.chart.OptionDealerAvgUnitValuePlot;
import com.javatican.stock.option.chart.OptionDealerOIUnitPlot;
import com.javatican.stock.option.chart.OptionDealerOIValuePlot;
import com.javatican.stock.option.chart.OptionForeignAvgUnitValuePlot;
import com.javatican.stock.option.chart.OptionForeignOIUnitPlot;
import com.javatican.stock.option.chart.OptionForeignOIValuePlot;
import com.javatican.stock.option.chart.OptionOIPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthCallOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthCallOIPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthPutOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthPutOIPlot;

import com.javatican.stock.option.chart.OptionTopTradersCurrentWeekCallOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentWeekCallOIPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentWeekOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentWeekPutOINetPlot;
import com.javatican.stock.option.chart.OptionTopTradersCurrentWeekPutOIPlot;
import com.javatican.stock.util.StockChartUtils;

//
@Service("optionChartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class OptionChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String OPTION_CHART_RESOURCE = "file:./charts/option/*.png";
	private static final String OPTION_CHART_MAIN_RESOURCE_FILE_PATH = "file:./charts/option/option_%s.png";
	private static final String OPTION_CHART_STRATEGY_RESOURCE_FILE_PATH = "file:./charts/option/option_strategy_%s.png";
	private static final String OPTION_CHART_STRATEGY_RESOURCE2_FILE_PATH = "file:./charts/option/option_strategy2_%s.png";

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ResourcePatternResolver resourcePatternResolver;
	@Autowired
	private TradingDateDAO tradingDateDAO;
	@Autowired
	private FuturePricePlot futurePricePlot;
	@Autowired
	private OptionOIPlot optionOIPlot;
	@Autowired
	private OptionForeignOIValuePlot optionForeignOIValuePlot;
	@Autowired
	private OptionForeignOIUnitPlot optionForeignOIUnitPlot;
	@Autowired
	private OptionForeignAvgUnitValuePlot optionForeignAvgUnitValuePlot;
	@Autowired
	private OptionDealerOIValuePlot optionDealerOIValuePlot;
	@Autowired
	private OptionDealerOIUnitPlot optionDealerOIUnitPlot;
	@Autowired
	private OptionDealerAvgUnitValuePlot optionDealerAvgUnitValuePlot;
	// @Autowired
	// private OptionTrustOIValuePlot optionTrustOIValuePlot;
	// @Autowired
	// private OptionTrustOIUnitPlot optionTrustOIUnitPlot;
	// @Autowired
	// private OptionTrustAvgUnitValuePlot optionTrustAvgUnitValuePlot;

	@Autowired
	private OptionTopTradersCurrentMonthCallOIPlot optionTopTradersCurrentMonthCallOIPlot;
	@Autowired
	private OptionTopTradersCurrentMonthCallOINetPlot optionTopTradersCurrentMonthCallOINetPlot;
	@Autowired
	private OptionTopTradersCurrentMonthPutOIPlot optionTopTradersCurrentMonthPutOIPlot;
	@Autowired
	private OptionTopTradersCurrentMonthPutOINetPlot optionTopTradersCurrentMonthPutOINetPlot;
	@Autowired
	private OptionTopTradersCurrentMonthOINetPlot optionTopTradersCurrentMonthOINetPlot;

	@Autowired
	private OptionTopTradersCurrentWeekCallOIPlot optionTopTradersCurrentWeekCallOIPlot;
	@Autowired
	private OptionTopTradersCurrentWeekCallOINetPlot optionTopTradersCurrentWeekCallOINetPlot;
	@Autowired
	private OptionTopTradersCurrentWeekPutOIPlot optionTopTradersCurrentWeekPutOIPlot;
	@Autowired
	private OptionTopTradersCurrentWeekPutOINetPlot optionTopTradersCurrentWeekPutOINetPlot;
	@Autowired
	private OptionTopTradersCurrentWeekOINetPlot optionTopTradersCurrentWeekOINetPlot;

	public boolean createGraph(boolean force, String dateString, Date dateSince) {
		StockChartUtil sc = new StockChartUtil(dateString);
		try {
			sc.create(force, dateSince);
			return true;
		} catch (StockException e) {
			logger.info("Cannot create chart for option");
			return false;
		}
	}

	public class StockChartUtil {
		private String dateString;

		public StockChartUtil(String dateString) {
			this.dateString = dateString;

		}

		public String getDateString() {
			return dateString;
		}

		public void setDateString(String dateString) {
			this.dateString = dateString;
		}

		public boolean existsForLatestOptionChart() {
			Resource resource = resourceLoader
					.getResource(String.format(OPTION_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			return resource.exists();
		}

		public void create(boolean force, Date dateSince) throws StockException {
			if (force || !existsForLatestOptionChart()) {
				outputToPNG(dateSince);
			} else {
				logger.info("Latest stock chart exists for option ");
			}
		}

		private void deleteOldCharts() throws IOException {
			Resource[] resources = resourcePatternResolver.getResources(OPTION_CHART_RESOURCE);
			for (Resource r : resources) {
				r.getFile().delete();
			}
		}

		private void outputToPNG(Date dateSince) throws StockException {
			try {
				deleteOldCharts();
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			JFreeChart chart = createChart(dateSince);
			Resource resource = resourceLoader
					.getResource(String.format(OPTION_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating main chart for option using latest data dated: " + dateString);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			// Resource resource = resourceLoader
			chart = createStrategyChart(dateSince);
			resource = resourceLoader.getResource(String.format(OPTION_CHART_STRATEGY_RESOURCE_FILE_PATH, dateString));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating strategy chart for option using latest data dated: " + dateString);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			//
			chart = createStrategy2Chart(dateSince);
			resource = resourceLoader.getResource(String.format(OPTION_CHART_STRATEGY_RESOURCE2_FILE_PATH, dateString));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating strategy chart for option using latest data dated: " + dateString);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
		}

		private JFreeChart createChart(Date dateSince) throws StockException {
			XYPlot candlestickSubplot = (XYPlot) futurePricePlot.getPlot();
			XYPlot oiSubplot = (XYPlot) optionOIPlot.getPlot(dateSince);
			XYPlot foreignOIValueSubplot = (XYPlot) optionForeignOIValuePlot.getPlot(dateSince);
			XYPlot foreignOIUnitSubplot = (XYPlot) optionForeignOIUnitPlot.getPlot(dateSince);
			XYPlot foreignOIAvgValueSubplot = (XYPlot) optionForeignAvgUnitValuePlot.getPlot(dateSince);
			XYPlot dealerOIValueSubplot = (XYPlot) optionDealerOIValuePlot.getPlot(dateSince);
			XYPlot dealerOIUnitSubplot = (XYPlot) optionDealerOIUnitPlot.getPlot(dateSince);
			XYPlot dealerOIAvgValueSubplot = (XYPlot) optionDealerAvgUnitValuePlot.getPlot(dateSince);
			// XYPlot trustOIValueSubplot = (XYPlot)
			// optionTrustOIValuePlot.getPlot(dateSince);
			// XYPlot trustOIUnitSubplot = (XYPlot) optionTrustOIUnitPlot.getPlot();
			// XYPlot trustOIAvgValueSubplot = (XYPlot)
			// optionTrustAvgUnitValuePlot.getPlot();
			//

			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(5.0);
			// annotation x position
			double x = candlestickSubplot.getDataset(0).getX(0, 0).doubleValue();
			//
			mainPlot.add(candlestickSubplot, 6);
			mainPlot.add(oiSubplot, 2);
			mainPlot.add(foreignOIValueSubplot, 2);
			mainPlot.add(foreignOIUnitSubplot, 2);
			mainPlot.add(foreignOIAvgValueSubplot, 2);
			mainPlot.add(dealerOIValueSubplot, 2);
			mainPlot.add(dealerOIUnitSubplot, 2);
			mainPlot.add(dealerOIAvgValueSubplot, 2);
			// mainPlot.add(trustOIValueSubplot, 2);
			// mainPlot.add(trustOIUnitSubplot, 2);
			// mainPlot.add(trustOIAvgValueSubplot, 2);
			//
			StockChartUtils.showAnnotation(oiSubplot, x, "OI");
			StockChartUtils.showAnnotation(foreignOIValueSubplot, x, "外资OI契约金额");
			StockChartUtils.showAnnotation(foreignOIUnitSubplot, x, "外资OI口数");
			StockChartUtils.showAnnotation(foreignOIAvgValueSubplot, x, "外资平均OI金額");
			StockChartUtils.showAnnotation(dealerOIValueSubplot, x, "自營商OI契约金额");
			StockChartUtils.showAnnotation(dealerOIUnitSubplot, x, "自營商OI口数");
			StockChartUtils.showAnnotation(dealerOIAvgValueSubplot, x, "自營商平均OI金額");
			// StockChartUtils.showAnnotation(trustOIValueSubplot, x, "投信OI契约金额");
			// StockChartUtils.showAnnotation(trustOIUnitSubplot, x, "投信OI口数");
			// StockChartUtils.showAnnotation(trustOIAvgValueSubplot, x, "投信平均OI金額");
			//
			List<Date> futureClosingDateList = tradingDateDAO.getAllFutureClosingDates();
			StockChartUtils.drawVerticalValueMarkersForDateList(candlestickSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(oiSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(foreignOIValueSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(foreignOIUnitSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(foreignOIAvgValueSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(dealerOIValueSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(dealerOIUnitSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(dealerOIAvgValueSubplot, futureClosingDateList);
			// StockChartUtils.drawVerticalValueMarkersForDateList(trustOIValueSubplot,
			// futureClosingDateList);
			// StockChartUtils.drawVerticalValueMarkersForDateList(trustOIUnitSubplot,
			// futureClosingDateList);
			// StockChartUtils.drawVerticalValueMarkersForDateList(trustOIAvgValueSubplot,
			// futureClosingDateList);
			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Option chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

		private JFreeChart createStrategyChart(Date dateSince) throws StockException {
			XYPlot candlestickSubplot = (XYPlot) futurePricePlot.getPlot();
			XYPlot topTradersCurrentMonthCallOiSubplot = (XYPlot) optionTopTradersCurrentMonthCallOIPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentMonthCallOiNetSubplot = (XYPlot) optionTopTradersCurrentMonthCallOINetPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentMonthPutOiSubplot = (XYPlot) optionTopTradersCurrentMonthPutOIPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentMonthPutOiNetSubplot = (XYPlot) optionTopTradersCurrentMonthPutOINetPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentMonthOiNetSubplot = (XYPlot) optionTopTradersCurrentMonthOINetPlot
					.getPlot(dateSince);

			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(5.0);
			// annotation x position
			double x = candlestickSubplot.getDataset(0).getX(0, 0).doubleValue();
			//
			mainPlot.add(candlestickSubplot, 6);
			mainPlot.add(topTradersCurrentMonthCallOiSubplot, 2);
			mainPlot.add(topTradersCurrentMonthCallOiNetSubplot, 2);
			mainPlot.add(topTradersCurrentMonthPutOiSubplot, 2);
			mainPlot.add(topTradersCurrentMonthPutOiNetSubplot, 2);
			mainPlot.add(topTradersCurrentMonthOiNetSubplot, 2);
			//
			StockChartUtils.showAnnotation(topTradersCurrentMonthCallOiSubplot, x, "大额交易人买權买卖(本月)");
			StockChartUtils.showAnnotation(topTradersCurrentMonthCallOiNetSubplot, x, "大额交易人买權买卖差(本月)");
			StockChartUtils.showAnnotation(topTradersCurrentMonthPutOiSubplot, x, "大额交易人卖權买卖(本月)");
			StockChartUtils.showAnnotation(topTradersCurrentMonthPutOiNetSubplot, x, "大额交易人卖權买卖差(本月)");
			StockChartUtils.showAnnotation(topTradersCurrentMonthOiNetSubplot, x, "大额交易人买權卖權买卖差(本月)");

			//
			List<Date> futureClosingDateList = tradingDateDAO.getAllFutureClosingDates();
			StockChartUtils.drawVerticalValueMarkersForDateList(candlestickSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthCallOiSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthCallOiNetSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthPutOiSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthPutOiNetSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthOiNetSubplot,
					futureClosingDateList);

			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Option chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

		private JFreeChart createStrategy2Chart(Date dateSince) throws StockException {
			XYPlot candlestickSubplot = (XYPlot) futurePricePlot.getPlot();
			XYPlot topTradersCurrentWeekCallOiSubplot = (XYPlot) optionTopTradersCurrentWeekCallOIPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentWeekCallOiNetSubplot = (XYPlot) optionTopTradersCurrentWeekCallOINetPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentWeekPutOiSubplot = (XYPlot) optionTopTradersCurrentWeekPutOIPlot.getPlot(dateSince);
			XYPlot topTradersCurrentWeekPutOiNetSubplot = (XYPlot) optionTopTradersCurrentWeekPutOINetPlot
					.getPlot(dateSince);
			XYPlot topTradersCurrentWeekOiNetSubplot = (XYPlot) optionTopTradersCurrentWeekOINetPlot.getPlot(dateSince);

			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(5.0);
			// annotation x position
			double x = candlestickSubplot.getDataset(0).getX(0, 0).doubleValue();
			//
			mainPlot.add(candlestickSubplot, 6);
			mainPlot.add(topTradersCurrentWeekCallOiSubplot, 2);
			mainPlot.add(topTradersCurrentWeekCallOiNetSubplot, 2);
			mainPlot.add(topTradersCurrentWeekPutOiSubplot, 2);
			mainPlot.add(topTradersCurrentWeekPutOiNetSubplot, 2);
			mainPlot.add(topTradersCurrentWeekOiNetSubplot, 2);
			//
			StockChartUtils.showAnnotation(topTradersCurrentWeekCallOiSubplot, x, "大额交易人买權买卖(本周)");
			StockChartUtils.showAnnotation(topTradersCurrentWeekCallOiNetSubplot, x, "大额交易人买權买卖差(本周)");
			StockChartUtils.showAnnotation(topTradersCurrentWeekPutOiSubplot, x, "大额交易人卖權买卖(本周)");
			StockChartUtils.showAnnotation(topTradersCurrentWeekPutOiNetSubplot, x, "大额交易人卖權买卖差(本周)");
			StockChartUtils.showAnnotation(topTradersCurrentWeekOiNetSubplot, x, "大额交易人买權卖權买卖差(本周)");

			//
			List<Date> futureClosingDateList = tradingDateDAO.getAllFutureClosingDates();
			StockChartUtils.drawVerticalValueMarkersForDateList(candlestickSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentWeekCallOiSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentWeekCallOiNetSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentWeekPutOiSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentWeekPutOiNetSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentWeekOiNetSubplot,
					futureClosingDateList);

			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Option chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}
	}
}
package com.javatican.stock.service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
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
import com.javatican.stock.future.chart.FutureDealerOIPlot;
import com.javatican.stock.future.chart.FutureForeignOIPlot;
import com.javatican.stock.future.chart.FutureOINextMonthPlot;
import com.javatican.stock.future.chart.FutureOIPlot;
import com.javatican.stock.future.chart.FutureOthersOIPlot;
import com.javatican.stock.future.chart.FuturePricePlot;
import com.javatican.stock.future.chart.FutureTopTradersCurrentMonthOINetPlot;
import com.javatican.stock.future.chart.FutureTopTradersCurrentMonthOIPlot;
import com.javatican.stock.future.chart.FutureTopTradersOINetPlot;
import com.javatican.stock.future.chart.FutureTopTradersOIPlot;
import com.javatican.stock.future.chart.FutureTrustOIPlot;
import com.javatican.stock.util.StockChartUtils;

@Service("futureChartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class FutureChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String FUTURE_CHART_RESOURCE = "file:./charts/future/*.png";
	private static final String FUTURE_CHART_MAIN_RESOURCE_FILE_PATH = "file:./charts/future/future_%s.png";
	// private static final String FUTURE_CHART_STRATEGY_RESOURCE_FILE_PATH =
	// "file:./charts/strategy/future_%s.png";

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ResourcePatternResolver resourcePatternResolver;
	@Autowired
	private TradingDateDAO tradingDateDAO;
	@Autowired
	private FuturePricePlot futurePricePlot;
	@Autowired
	private FutureOIPlot futureOIPlot;
	@Autowired
	private FutureOINextMonthPlot futureOINextMonthPlot;
	@Autowired
	private FutureForeignOIPlot futureForeignOIPlot;
	@Autowired
	private FutureTrustOIPlot futureTrustOIPlot;
	@Autowired
	private FutureDealerOIPlot futureDealerOIPlot;
	@Autowired
	private FutureOthersOIPlot futureOthersOIPlot;
	@Autowired
	private FutureTopTradersCurrentMonthOIPlot futureTopTradersCurrentMonthOIPlot;
	@Autowired
	private FutureTopTradersOIPlot futureTopTradersOIPlot;

	@Autowired
	private FutureTopTradersOINetPlot futureTopTradersOINetPlot;
	@Autowired
	private FutureTopTradersCurrentMonthOINetPlot futureTopTradersCurrentMonthOINetPlot;

	public boolean createGraph(boolean force, String dateString) {
		StockChartUtil sc = new StockChartUtil(dateString);
		try {
			sc.create(force);
			return true;
		} catch (StockException e) {
			logger.info("Cannot create chart for future");
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

		public boolean existsForLatestFutureChart() {
			Resource resource = resourceLoader
					.getResource(String.format(FUTURE_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			return resource.exists();
		}

		public void create(boolean force) throws StockException {
			if (force || !existsForLatestFutureChart()) {
				outputToPNG();
			} else {
				logger.info("Latest stock chart exists for future ");
			}
		}

		private void deleteOldCharts() throws IOException {
			Resource[] resources = resourcePatternResolver.getResources(FUTURE_CHART_RESOURCE);
			for (Resource r : resources) {
				r.getFile().delete();
			}
		}

		private void outputToPNG() throws StockException {
			try {
				deleteOldCharts();
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			JFreeChart chart = createChart();
			Resource resource = resourceLoader
					.getResource(String.format(FUTURE_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating main chart for future using latest data dated: " + dateString);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			// Resource resource = resourceLoader
			// chart = createStrategyChart();
			// resource = resourceLoader
			// .getResource(String.format(INDEX_CHART_STRATEGY_RESOURCE_FILE_PATH,
			// dateString));
			// try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			// ChartUtils.writeChartAsPNG(st, chart, 1600, 1260);
			// logger.info("Finish creating strategy chart for future using latest data
			// dated: " + dateString);
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// throw new StockException(ex);
			// }
		}

		private JFreeChart createChart() throws StockException {
			XYPlot candlestickSubplot = (XYPlot) futurePricePlot.getPlot();
			XYPlot oiSubplot = (XYPlot) futureOIPlot.getPlot();
			XYPlot oiNextMonthSubplot = (XYPlot) futureOINextMonthPlot.getPlot();
			XYPlot foreignSubplot = (XYPlot) futureForeignOIPlot.getPlot();
			XYPlot trustSubplot = (XYPlot) futureTrustOIPlot.getPlot();
			XYPlot dealerSubplot = (XYPlot) futureDealerOIPlot.getPlot();
			XYPlot othersSubplot = (XYPlot) futureOthersOIPlot.getPlot();
			XYPlot topTradersCurrentMonthOiSubplot = (XYPlot) futureTopTradersCurrentMonthOIPlot.getPlot();
			XYPlot topTradersCurrentMonthOiNetSubplot = (XYPlot) futureTopTradersCurrentMonthOINetPlot.getPlot();
			XYPlot topTradersOiSubplot = (XYPlot) futureTopTradersOIPlot.getPlot();
			XYPlot topTradersOiNetSubplot = (XYPlot) futureTopTradersOINetPlot.getPlot();

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
			mainPlot.add(oiNextMonthSubplot, 2);
			mainPlot.add(foreignSubplot, 2);
			mainPlot.add(trustSubplot, 2);
			mainPlot.add(dealerSubplot, 2);
			mainPlot.add(othersSubplot, 2);
			mainPlot.add(topTradersCurrentMonthOiSubplot, 2);
			mainPlot.add(topTradersCurrentMonthOiNetSubplot, 2);
			mainPlot.add(topTradersOiSubplot, 2);
			mainPlot.add(topTradersOiNetSubplot, 2);
			//
			StockChartUtils.showAnnotation(oiSubplot, x, "本月份");
			StockChartUtils.showAnnotation(oiNextMonthSubplot, x, "下一月份");
			StockChartUtils.showAnnotation(foreignSubplot, x, "外资买卖");
			StockChartUtils.showAnnotation(trustSubplot, x, "投信买卖");
			StockChartUtils.showAnnotation(dealerSubplot, x, "自营商买卖");
			StockChartUtils.showAnnotation(othersSubplot, x, "其它买卖");
			StockChartUtils.showAnnotation(topTradersCurrentMonthOiSubplot, x, "大额交易人买卖(本月)");
			StockChartUtils.showAnnotation(topTradersCurrentMonthOiNetSubplot, x, "大额交易人买卖差(本月)");
			StockChartUtils.showAnnotation(topTradersOiSubplot, x, "大额交易人买卖(全)");
			StockChartUtils.showAnnotation(topTradersOiNetSubplot, x, "大额交易人买卖差(全)");

			//
			List<Date> futureClosingDateList = tradingDateDAO.getAllFutureClosingDates();
			StockChartUtils.drawVerticalValueMarkersForDateList(candlestickSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(oiSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(oiNextMonthSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(foreignSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(trustSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(dealerSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(othersSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthOiSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersCurrentMonthOiNetSubplot,
					futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersOiSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(topTradersOiNetSubplot, futureClosingDateList);

			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Future chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

	}
}
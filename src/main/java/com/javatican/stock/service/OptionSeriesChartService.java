package com.javatican.stock.service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
import com.javatican.stock.dao.OptionSeriesDataDAO;
import com.javatican.stock.dao.TradingDateDAO;
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.index.chart.IndexPricePlot;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.option.series.chart.OptionSeriesOIPlot;
import com.javatican.stock.option.series.chart.OptionSeriesPricePlot;
import com.javatican.stock.util.StockChartUtils;
import com.javatican.stock.util.StockUtils;

//
@Service("optionSeriesChartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class OptionSeriesChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String OPTION_SERIES_CHART_RESOURCE = "file:./charts/option/series/*.png";
	private static final String OPTION_SERIES_MAIN_CHART_RESOURCE_FILE_PATH = "file:./charts/option/series/option_%s.png";
	// eg. option_call_11000_20180928.png for call option with strike price of 11000
	// and using data until 20180928
	private static final String OPTION_SERIES_CHART_RESOURCE_FILE_PATH = "file:./charts/option/series/option_%s_%s_%s.png";

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ResourcePatternResolver resourcePatternResolver;
	@Autowired
	private TradingValueDAO tradingValueDAO;
	@Autowired
	private TradingDateDAO tradingDateDAO;
	@Autowired
	private OptionSeriesDataDAO optionSeriesDataDAO;
	@Autowired
	private IndexPricePlot indexPricePlot;
	@Autowired
	private OptionSeriesPricePlot weekOptionSeriesPricePlot;
	@Autowired
	private OptionSeriesOIPlot weekOptionSeriesOIPlot;
	@Autowired
	private OptionSeriesPricePlot currentMonthOptionSeriesPricePlot;
	@Autowired
	private OptionSeriesOIPlot currentMonthOptionSeriesOIPlot;
	@Autowired
	private OptionSeriesPricePlot nextMonthOptionSeriesPricePlot;
	@Autowired
	private OptionSeriesOIPlot nextMonthOptionSeriesOIPlot;

	public boolean createGraph(boolean force, String dateString, Date dateSince, Integer upRange, Integer downRange) {
		StockChartUtil sc = new StockChartUtil(dateString);
		try {
			sc.create(force, dateSince, upRange, downRange);
			return true;
		} catch (StockException e) {
			logger.info("Cannot create option series chart.");
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
					.getResource(String.format(OPTION_SERIES_MAIN_CHART_RESOURCE_FILE_PATH, dateString));
			return resource.exists();
		}

		public void create(boolean force, Date dateSince, Integer upRange, Integer downRange) throws StockException {
			if (force || !existsForLatestOptionChart()) {
				outputToPNG(dateSince, upRange, downRange);
			} else {
				logger.info("Latest stock option series chart exists.");
			}
		}

		private void deleteOldCharts() throws IOException {
			Resource[] resources = resourcePatternResolver.getResources(OPTION_SERIES_CHART_RESOURCE);
			for (Resource r : resources) {
				r.getFile().delete();
			}
		}

		private void outputToPNG(Date dateSince, Integer upRange, Integer downRange) throws StockException {
			try {
				deleteOldCharts();
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			//
			TradingValue tv = tradingValueDAO.getByTradingDate(StockUtils.stringSimpleToDate(dateString).get());
			List<Integer> callOptionStrikePriceRange = optionSeriesDataDAO.findCallOptionStrikePriceBetween(
					(int) (tv.getClose() - downRange), (int) (tv.getClose() + upRange));
			List<Integer> putOptionStrikePriceRange = optionSeriesDataDAO.findPutOptionStrikePriceBetween(
					(int) (tv.getClose() - upRange), (int) (tv.getClose() + downRange));
			// call option series
			boolean mainChartCreated = false;
			JFreeChart chart = null;
			Resource resource = null;
			for (Integer sp : callOptionStrikePriceRange) {
				chart = createChart(dateSince, true, sp);
				resource = resourceLoader
						.getResource(String.format(OPTION_SERIES_CHART_RESOURCE_FILE_PATH, "call", sp, dateString));
				try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
					ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
					logger.info("Finish creating call option series chart for " + sp + " using latest data dated: "
							+ dateString);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new StockException(ex);
				}
				if (!mainChartCreated && sp >= tv.getClose()) {
					mainChartCreated = true;
					resource = resourceLoader
							.getResource(String.format(OPTION_SERIES_MAIN_CHART_RESOURCE_FILE_PATH, dateString));
					try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
						ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
						logger.info("Finish creating main series chart using latest data dated: " + dateString);
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new StockException(ex);
					}
				}
			}
			// put option series
			for (Integer sp : putOptionStrikePriceRange) {
				chart = createChart(dateSince, false, sp);
				resource = resourceLoader
						.getResource(String.format(OPTION_SERIES_CHART_RESOURCE_FILE_PATH, "put", sp, dateString));
				try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
					ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
					logger.info("Finish creating put option series chart for " + sp + " using latest data dated: "
							+ dateString);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new StockException(ex);
				}
			}
		}

		private JFreeChart createChart(Date dateSince, boolean callOption, Integer strikePrice) throws StockException {

			XYPlot candlestickSubplot = (XYPlot) indexPricePlot.getPlot();
			// week option
			XYPlot weekPriceSubplot = (XYPlot) weekOptionSeriesPricePlot.getPlot(dateSince, callOption, strikePrice,
					"week");
			XYPlot weekOISubplot = (XYPlot) weekOptionSeriesOIPlot.getPlot(dateSince, callOption, strikePrice, "week");
			// currentMonth option
			XYPlot currentMonthPriceSubplot = (XYPlot) currentMonthOptionSeriesPricePlot.getPlot(dateSince, callOption,
					strikePrice, "currentMonth");
			XYPlot currentMonthOISubplot = (XYPlot) currentMonthOptionSeriesOIPlot.getPlot(dateSince, callOption,
					strikePrice, "currentMonth");
			// nextMonth option
			XYPlot nextMonthPriceSubplot = (XYPlot) nextMonthOptionSeriesPricePlot.getPlot(dateSince, callOption,
					strikePrice, "nextMonth");
			XYPlot nextMonthOISubplot = (XYPlot) nextMonthOptionSeriesOIPlot.getPlot(dateSince, callOption, strikePrice,
					"nextMonth");

			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(5.0);
			// annotation x position
			double x = candlestickSubplot.getDataset(0).getX(0, 0).doubleValue();
			//
			mainPlot.add(candlestickSubplot, 2);
			mainPlot.add(weekPriceSubplot, 2);
			mainPlot.add(weekOISubplot, 2);
			mainPlot.add(currentMonthPriceSubplot, 2);
			mainPlot.add(currentMonthOISubplot, 2);
			mainPlot.add(nextMonthPriceSubplot, 2);
			mainPlot.add(nextMonthOISubplot, 2);
			//
			StockChartUtils.showAnnotation(candlestickSubplot, x, "大盘指数");
			StockChartUtils.showAnnotation(weekPriceSubplot, x, "选择權价格(周)");
			StockChartUtils.showAnnotation(weekOISubplot, x, "选择權OI口数(周)");
			StockChartUtils.showAnnotation(currentMonthPriceSubplot, x, "选择權价格(本月)");
			StockChartUtils.showAnnotation(currentMonthOISubplot, x, "选择權OI口数(本月)");
			StockChartUtils.showAnnotation(nextMonthPriceSubplot, x, "选择權价格(下月)");
			StockChartUtils.showAnnotation(nextMonthOISubplot, x, "选择權OI口数(下月)");
			//
			List<Date> futureClosingDateList = tradingDateDAO.getAllFutureClosingDates();
			StockChartUtils.drawHorizontalValueMarker(candlestickSubplot, 0, strikePrice);
			StockChartUtils.drawVerticalValueMarkersForDateList(candlestickSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(weekPriceSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(weekOISubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(currentMonthPriceSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(currentMonthOISubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(nextMonthPriceSubplot, futureClosingDateList);
			StockChartUtils.drawVerticalValueMarkersForDateList(nextMonthOISubplot, futureClosingDateList);

			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("%s选择權线图(%s,履约价%s)", dateString, callOption?"买權":"卖權", strikePrice),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

	}
}
package com.javatican.stock.service;

import java.io.OutputStream;
import java.text.SimpleDateFormat;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.future.chart.FuturePricePlot;
import com.javatican.stock.option.chart.OptionForeignOIUnitPlot;
import com.javatican.stock.option.chart.OptionForeignOIValuePlot;
import com.javatican.stock.option.chart.OptionOIPlot;
import com.javatican.stock.util.StockChartUtils;
//import com.javatican.stock.option.chart.OptionDealerOIPlot;
//import com.javatican.stock.option.chart.OptionForeignOIPlot;
//import com.javatican.stock.option.chart.OptionOthersOIPlot;
//import com.javatican.stock.option.chart.OptionPricePlot;
//import com.javatican.stock.option.chart.OptionTopTradersCurrentMonthOIPlot;
//import com.javatican.stock.option.chart.OptionTopTradersOIPlot;
//import com.javatican.stock.option.chart.OptionTrustOIPlot;
//
@Service("optionChartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class OptionChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String OPTION_CHART_MAIN_RESOURCE_FILE_PATH = "file:./charts/option_%s.png";
	private static final String OPTION_CHART_STRATEGY_RESOURCE_FILE_PATH = "file:./charts/strategy/option_%s.png";

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private FuturePricePlot futurePricePlot;
	@Autowired
	private OptionOIPlot optionOIPlot; 
	@Autowired
	private OptionForeignOIValuePlot optionForeignOIValuePlot;
	@Autowired
	private OptionForeignOIUnitPlot optionForeignOIUnitPlot;
//	@Autowired
//	private OptionTrustOIPlot optionTrustOIPlot;
//	@Autowired
//	private OptionDealerOIPlot optionDealerOIPlot;
//	@Autowired
//	private OptionOthersOIPlot optionOthersOIPlot;
//	@Autowired
//	private OptionTopTradersCurrentMonthOIPlot optionTopTradersCurrentMonthOIPlot;
//	@Autowired
//	private OptionTopTradersOIPlot optionTopTradersOIPlot;

	public boolean createGraph(boolean force, String dateString) {
		StockChartUtil sc = new StockChartUtil(dateString);
		try {
			sc.create(force);
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

		public void create(boolean force) throws StockException {
			if (force || !existsForLatestOptionChart()) {
				outputToPNG();
			} else {
				logger.info("Latest stock chart exists for option ");
			}
		}

		private void outputToPNG() throws StockException {
			JFreeChart chart = createChart();
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
			// chart = createStrategyChart();
			// resource = resourceLoader
			// .getResource(String.format(INDEX_CHART_STRATEGY_RESOURCE_FILE_PATH,
			// dateString));
			// try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			// ChartUtils.writeChartAsPNG(st, chart, 1600, 1260);
			// logger.info("Finish creating strategy chart for option using latest data
			// dated: " + dateString);
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// throw new StockException(ex);
			// }
		}

		private JFreeChart createChart() throws StockException {
			XYPlot candlestickSubplot = (XYPlot) futurePricePlot.getPlot();
			XYPlot oiSubplot = (XYPlot) optionOIPlot.getPlot(); 
			XYPlot foreignOIValueSubplot = (XYPlot) optionForeignOIValuePlot.getPlot();
			XYPlot foreignOIUnitSubplot = (XYPlot) optionForeignOIUnitPlot.getPlot();
//			XYPlot trustSubplot = (XYPlot) optionTrustOIPlot.getPlot();
//			XYPlot dealerSubplot = (XYPlot) optionDealerOIPlot.getPlot();
//			XYPlot othersSubplot = (XYPlot) optionOthersOIPlot.getPlot();
//			XYPlot topTradersCurrentMonthSubplot = (XYPlot) optionTopTradersCurrentMonthOIPlot.getPlot();
//			XYPlot topTradersSubplot = (XYPlot) optionTopTradersOIPlot.getPlot();

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
//			mainPlot.add(trustSubplot, 2);
//			mainPlot.add(dealerSubplot, 2);
//			mainPlot.add(othersSubplot, 2);
//			mainPlot.add(topTradersCurrentMonthSubplot, 2);
//			mainPlot.add(topTradersSubplot, 2);
//			//
			StockChartUtils.showAnnotation(oiSubplot, x, "OI"); 
			StockChartUtils.showAnnotation(foreignOIValueSubplot, x, "外资OI契约金额");
			StockChartUtils.showAnnotation(foreignOIUnitSubplot, x, "外资OI口数");
//			StockChartUtils.showAnnotation(trustSubplot, x, "投信买卖");
//			StockChartUtils.showAnnotation(dealerSubplot, x, "自营商买卖");
//			StockChartUtils.showAnnotation(othersSubplot, x, "其它买卖");
//			StockChartUtils.showAnnotation(topTradersCurrentMonthSubplot, x, "大额交易人买卖(本月)");
//			StockChartUtils.showAnnotation(topTradersSubplot, x, "大额交易人买卖(全)");
//			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Option chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}
 

	}
}
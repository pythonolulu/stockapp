package com.javatican.stock.service;

import java.io.IOException;
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
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.index.chart.IndexDealerHedgeTradeVolumePlot;
import com.javatican.stock.index.chart.IndexDealerTradeVolumePlot;
import com.javatican.stock.index.chart.IndexForeignTradeVolumePlot;
import com.javatican.stock.index.chart.IndexMarginBuyPlot;
import com.javatican.stock.index.chart.IndexMarginBuyValuePlot;
import com.javatican.stock.index.chart.IndexMarginShortSellPlot;
import com.javatican.stock.index.chart.IndexPricePlot;
import com.javatican.stock.index.chart.IndexTrustTradeVolumePlot;
import com.javatican.stock.index.chart.IndexVolumePlot;
import com.javatican.stock.index.chart.SmaStatsPlot;
import com.javatican.stock.util.StockChartUtils;

@Service("indexChartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class IndexChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String INDEX_CHART_RESOURCE = "file:./charts/index/*.png";
	private static final String INDEX_CHART_MAIN_RESOURCE_FILE_PATH = "file:./charts/index/index_%s.png";
	//private static final String INDEX_CHART_STRATEGY_RESOURCE_FILE_PATH = "file:./charts/strategy/index_%s.png";

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ResourcePatternResolver resourcePatternResolver;
	@Autowired 
	private IndexPricePlot indexPricePlot;
	@Autowired 
	private IndexVolumePlot indexVolumePlot;
	@Autowired 
	private IndexForeignTradeVolumePlot indexForeignTradeVolumePlot;
	@Autowired 
	private IndexTrustTradeVolumePlot indexTrustTradeVolumePlot;
	@Autowired 
	private IndexDealerTradeVolumePlot indexDealerTradeVolumePlot;
	@Autowired 
	private IndexDealerHedgeTradeVolumePlot indexDealerHedgeTradeVolumePlot;
	@Autowired 
	private IndexMarginBuyValuePlot indexMarginBuyValuePlot;
	@Autowired 
	private IndexMarginBuyPlot indexMarginBuyPlot;
	@Autowired 
	private IndexMarginShortSellPlot indexMarginShortSellPlot;
	@Autowired 
	private SmaStatsPlot smaStatsPlot;

	public boolean createGraph(boolean force, String dateString) {
		StockChartUtil sc = new StockChartUtil(dateString);
		try {
			sc.create(force);
			return true;
		} catch (StockException e) {
			logger.info("Cannot create chart for index");
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

		public boolean existsForLatestIndexChart() {
			Resource resource = resourceLoader
					.getResource(String.format(INDEX_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			return resource.exists();
		}

		public void create(boolean force) throws StockException {
			if (force || !existsForLatestIndexChart()) {
				outputToPNG();
			} else {
				logger.info("Latest stock chart exists for index ");
			}
		}

		private void deleteOldCharts() throws IOException {
			Resource[] resources = resourcePatternResolver.getResources(INDEX_CHART_RESOURCE);
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
					.getResource(String.format(INDEX_CHART_MAIN_RESOURCE_FILE_PATH, dateString));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800); 
				logger.info("Finish creating main chart for index using latest data dated: " + dateString);
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
			// logger.info("Finish creating strategy chart for index using latest data
			// dated: " + dateString);
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// throw new StockException(ex);
			// }
		}

		private JFreeChart createChart() throws StockException {
			XYPlot candlestickSubplot = (XYPlot) indexPricePlot.getPlot();
			XYPlot volumeSubplot = (XYPlot) indexVolumePlot.getPlot();
			XYPlot foreignSubplot = (XYPlot) indexForeignTradeVolumePlot.getPlot();
			XYPlot trustSubplot = (XYPlot) indexTrustTradeVolumePlot.getPlot();
			XYPlot dealerSubplot = (XYPlot) indexDealerTradeVolumePlot.getPlot();
			XYPlot dealerHedgeSubplot = (XYPlot) indexDealerHedgeTradeVolumePlot.getPlot();
			XYPlot marginBuyValueSubplot = (XYPlot) indexMarginBuyValuePlot.getPlot();
			XYPlot marginBuySubplot = (XYPlot) indexMarginBuyPlot.getPlot();
			XYPlot marginShortSubplot = (XYPlot) indexMarginShortSellPlot.getPlot();
			XYPlot smaStatsSubplot = (XYPlot) smaStatsPlot.getPlot();

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
			mainPlot.add(volumeSubplot, 2);
			mainPlot.add(foreignSubplot, 2);
			mainPlot.add(trustSubplot, 2);
			mainPlot.add(dealerSubplot, 2);
			mainPlot.add(dealerHedgeSubplot, 2);
			mainPlot.add(marginBuyValueSubplot, 2);
			mainPlot.add(marginBuySubplot, 2);
			mainPlot.add(marginShortSubplot, 2);
			mainPlot.add(smaStatsSubplot, 2);
//
			StockChartUtils.showAnnotation(foreignSubplot, x, "外资买卖"); 
			StockChartUtils.showAnnotation(trustSubplot, x, "投信买卖");
			StockChartUtils.showAnnotation(dealerSubplot, x, "自营商买卖");
			StockChartUtils.showAnnotation(dealerHedgeSubplot, x, "自营商避险买卖");
			StockChartUtils.showAnnotation(marginBuyValueSubplot, x, "融资买卖金额");
			StockChartUtils.showAnnotation(marginBuySubplot, x, "融资买卖");
			StockChartUtils.showAnnotation(marginShortSubplot, x, "融券买卖");
			StockChartUtils.showAnnotation(smaStatsSubplot, x, "均线统计");
			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("Index chart : %s", dateString),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}


	}
}
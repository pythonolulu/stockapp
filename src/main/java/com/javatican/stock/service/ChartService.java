package com.javatican.stock.service;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.date.SerialDate;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.chart.JPlot;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.DealerTradeSummaryDAO;
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockItemDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.dao.StockItemLogDAO;
import com.javatican.stock.dao.StockTradeByForeignDAO;
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockItemLog;
import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.util.StockChartUtils;
import com.javatican.stock.util.StockUtils;

@Service("chartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class ChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String STOCK_CHART_MAIN_RESOURCE_FILE_PATH = "file:./charts/%s.png";
	private static final String STOCK_CHART_STRATEGY_RESOURCE_FILE_PATH = "file:./charts/strategy/%s.png";

	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;
	@Autowired
	PutWarrantTradeSummaryDAO putWarrantTradeSummaryDAO;
	@Autowired
	DealerTradeSummaryDAO dealerTradeSummaryDAO;
	@Autowired
	StockTradeByForeignDAO stockTradeByForeignDAO;
	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	@Autowired
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	StockItemDAO stockItemDAO;
	@Autowired
	StockItemLogDAO stockItemLogDAO;
	@Autowired
	StockItemHelper stockItemHelper;
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	@Qualifier("pricePlot")
	private JPlot pricePlot;
	@Autowired
	@Qualifier("volumePlot")
	private JPlot volumePlot;
	@Autowired
	@Qualifier("cwtvPlot")
	private JPlot cwtvPlot;
	@Autowired
	@Qualifier("ictvPlot")
	private JPlot ictvPlot;
	@Autowired
	@Qualifier("pwtvPlot")
	private JPlot pwtvPlot;
	@Autowired
	@Qualifier("iptvPlot")
	private JPlot iptvPlot;
	@Autowired
	@Qualifier("ftvPlot")
	private JPlot ftvPlot;
	@Autowired
	@Qualifier("ttvPlot")
	private JPlot ttvPlot;
	@Autowired
	@Qualifier("mbPlot")
	private JPlot mbPlot;
	@Autowired
	@Qualifier("mssPlot")
	private JPlot mssPlot;
	@Autowired
	@Qualifier("sblPlot")
	private JPlot sblPlot;
	@Autowired
	@Qualifier("cwss1Hp1Plot")
	private JPlot cwss1Hp1Plot;
	@Autowired
	@Qualifier("cwss1Hp3Plot")
	private JPlot cwss1Hp3Plot;
	@Autowired
	@Qualifier("cwss1Hp5Plot")
	private JPlot cwss1Hp5Plot;
	//
	@Autowired
	@Qualifier("pwss1Hp1Plot")
	private JPlot pwss1Hp1Plot;
	@Autowired
	@Qualifier("pwss1Hp3Plot")
	private JPlot pwss1Hp3Plot;
	@Autowired
	@Qualifier("pwss1Hp5Plot")
	private JPlot pwss1Hp5Plot;

	public void createGraphs(Collection<StockItem> siList, boolean force) {
		siList.stream().forEach(stockItem -> createGraph(stockItem, force));
	}

	public void createGraphs2(Collection<String> symbolList, boolean force) {
		symbolList.stream().forEach(symbol -> createGraph(symbol, force));
	}

	public boolean createGraph(StockItem stockItem, boolean force) {
		if (stockItem == null)
			return false;
		StockChartUtil sc = new StockChartUtil(stockItem);
		try {
			sc.create(force);
			return true;
		} catch (StockException e) {
			logger.info("Cannot create chart for " + stockItem.getSymbol());
			return false;
		}
	}

	public boolean createGraph(String stockSymbol, boolean force) {
		StockItem si = stockItemDAO.findBySymbol(stockSymbol);
		if (si != null) {
			return createGraph(si, force);
		} else {
			return false;
		}

	}

	public class StockChartUtil {
		private StockItem stockItem;
		private StockItemLog stockItemLog;

		public StockChartUtil(StockItem stockItem) {
			this.stockItem = stockItem;
			this.stockItemLog = stockItemLogDAO.findBySymbol(this.stockItem.getSymbol());
		}

		public void create(boolean force) throws StockException {
			if (force || needUpdateChartForSymbol()) {
				outputToPNG();
				stockItemHelper.updateChartDateForItem(stockItemLog.getSymbol(), stockItemLog.getPriceDate());
			} else {
				logger.info("Latest stock chart exists for " + stockItem.getSymbol());
			}
		}

		public boolean needUpdateChartForSymbol() {
			if (this.stockItemLog.getChartDate() == null
					|| this.stockItemLog.getChartDate().before(this.stockItemLog.getPriceDate()))
				return true;
			else
				return false;
		}

		private void outputToPNG() throws StockException {
			JFreeChart chart = createChart();
			Resource resource = resourceLoader
					.getResource(String.format(STOCK_CHART_MAIN_RESOURCE_FILE_PATH, stockItem.getSymbol()));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating main chart for " + stockItem.getSymbol() + " using latest data dated: "
						+ stockItemLog.getPriceDate());
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
			// Resource resource = resourceLoader
			chart = createStrategyChart();
			resource = resourceLoader
					.getResource(String.format(STOCK_CHART_STRATEGY_RESOURCE_FILE_PATH, stockItem.getSymbol()));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1260);
				logger.info("Finish creating strategy chart for " + stockItem.getSymbol() + " using latest data dated: "
						+ stockItemLog.getPriceDate());
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new StockException(ex);
			}
		}

		private JFreeChart createStrategyChart() throws StockException {
			XYPlot candlestickSubplot = (XYPlot) pricePlot.getPlot(stockItem);
			XYPlot volumeSubplot = (XYPlot) volumePlot.getPlot(stockItem);
			XYPlot cwss1Hp1Subplot = (XYPlot) cwss1Hp1Plot.getPlot(stockItem);
			XYPlot cwss1Hp3Subplot = (XYPlot) cwss1Hp3Plot.getPlot(stockItem);
			XYPlot cwss1Hp5Subplot = (XYPlot) cwss1Hp5Plot.getPlot(stockItem);
			//
			XYPlot pwss1Hp1Subplot = (XYPlot) pwss1Hp1Plot.getPlot(stockItem);
			XYPlot pwss1Hp3Subplot = (XYPlot) pwss1Hp3Plot.getPlot(stockItem);
			XYPlot pwss1Hp5Subplot = (XYPlot) pwss1Hp5Plot.getPlot(stockItem);

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

			if (cwss1Hp1Subplot != null) {
				mainPlot.add(cwss1Hp1Subplot, 2);
				StockChartUtils.showAnnotation(cwss1Hp1Subplot, x, "購權1日");
			}
			if (cwss1Hp3Subplot != null) {
				mainPlot.add(cwss1Hp3Subplot, 2);
				StockChartUtils.showAnnotation(cwss1Hp3Subplot, x, "購權3日");
			}
			if (cwss1Hp5Subplot != null) {
				mainPlot.add(cwss1Hp5Subplot, 2);
				StockChartUtils.showAnnotation(cwss1Hp5Subplot, x, "購權5日");
			}
			//
			if (pwss1Hp1Subplot != null) {
				mainPlot.add(pwss1Hp1Subplot, 2);
				StockChartUtils.showAnnotation(pwss1Hp1Subplot, x, "售權1日");
			}
			if (pwss1Hp3Subplot != null) {
				mainPlot.add(pwss1Hp3Subplot, 2);
				StockChartUtils.showAnnotation(pwss1Hp3Subplot, x, "售權3日");
			}
			if (pwss1Hp5Subplot != null) {
				mainPlot.add(pwss1Hp5Subplot, 2);
				StockChartUtils.showAnnotation(pwss1Hp5Subplot, x, "售權5日");
			}
			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("%s(%s)", stockItem.getName(), stockItem.getSymbol()),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

		private JFreeChart createChart() throws StockException {
			XYPlot candlestickSubplot = (XYPlot) pricePlot.getPlot(stockItem);
			XYPlot volumeSubplot = (XYPlot) volumePlot.getPlot(stockItem);
			XYPlot cwtvSubplot = (XYPlot) cwtvPlot.getPlot(stockItem);
			XYPlot ictvSubplot = null;
			if (cwtvSubplot != null) {
				ictvSubplot = (XYPlot) ictvPlot.getPlot(stockItem);
			}
			XYPlot pwtvSubplot = (XYPlot) pwtvPlot.getPlot(stockItem);
			if (cwtvSubplot != null && pwtvSubplot != null) {
				pwtvSubplot.getRenderer(0).setSeriesVisibleInLegend(0, false);
				pwtvSubplot.getRenderer(1).setSeriesVisibleInLegend(0, false);
				pwtvSubplot.getRenderer(2).setSeriesVisibleInLegend(0, false);
			}
			XYPlot iptvSubplot = null;
			if (pwtvSubplot != null) {
				iptvSubplot = (XYPlot) iptvPlot.getPlot(stockItem);
			}
			XYPlot ftvSubplot = (XYPlot) ftvPlot.getPlot(stockItem);
			XYPlot ttvSubplot = (XYPlot) ttvPlot.getPlot(stockItem);
			//
			XYPlot mbSubplot = (XYPlot) mbPlot.getPlot(stockItem);
			XYPlot mssSubplot = (XYPlot) mssPlot.getPlot(stockItem);
			XYPlot sblSubplot = (XYPlot) sblPlot.getPlot(stockItem);
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
			mainPlot.add(volumeSubplot, 2);
			if (ftvSubplot != null) {
				mainPlot.add(ftvSubplot, 2);
				StockChartUtils.showAnnotation(ftvSubplot, x, "外资买卖");
			}
			if (ttvSubplot != null) {
				mainPlot.add(ttvSubplot, 2);
				StockChartUtils.showAnnotation(ttvSubplot, x, "投信买卖");
			}
			if (mbSubplot != null) {
				mainPlot.add(mbSubplot, 2);
				StockChartUtils.showAnnotation(mbSubplot, x, "融资馀额");
			}
			if (mssSubplot != null) {
				mainPlot.add(mssSubplot, 2);
				StockChartUtils.showAnnotation(mssSubplot, x, "融券馀额");
			}
			if (sblSubplot != null) {
				mainPlot.add(sblSubplot, 2);
				StockChartUtils.showAnnotation(sblSubplot, x, "借券馀额");
			}
			if (cwtvSubplot != null) {
				mainPlot.add(cwtvSubplot, 2);
				StockChartUtils.showAnnotation(cwtvSubplot, x, "認購交易");
			}
			if (ictvSubplot != null) {
				mainPlot.add(ictvSubplot, 2);
				StockChartUtils.showAnnotation(ictvSubplot, x, "認購买卖");
			}
			//
			if (pwtvSubplot != null) {
				mainPlot.add(pwtvSubplot, 2);
				StockChartUtils.showAnnotation(pwtvSubplot, x, "認售交易");
			}
			if (iptvSubplot != null) {
				mainPlot.add(iptvSubplot, 2);
				StockChartUtils.showAnnotation(iptvSubplot, x, "認售买卖");
			}
			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("%s(%s)", stockItem.getName(), stockItem.getSymbol()),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}
 

	}
}
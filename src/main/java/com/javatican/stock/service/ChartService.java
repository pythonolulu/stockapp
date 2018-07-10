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
import com.javatican.stock.util.StockUtils;

@Service("chartService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class ChartService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String STOCK_CHART_RESOURCE_FILE_PATH = "file:./charts/%s.png";

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

	public void createGraphs(Collection<StockItem> siList) {
		siList.stream().forEach(stockItem -> createGraph(stockItem));
	}

	public boolean createGraph(StockItem stockItem) {
		if (stockItem == null)
			return false;
		StockChartUtil sc = new StockChartUtil(stockItem);
		try {
			sc.create();
			return true;
		} catch (StockException e) {
			logger.info("Cannot create chart for " + stockItem.getSymbol());
			return false;
		}
	}

	public boolean createGraph(String stockSymbol) {
		StockItem si = stockItemDAO.findBySymbol(stockSymbol);
		if (si != null) {
			createGraph(si);
			return true;
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

		public void create() throws StockException {
			if (needUpdateChartForSymbol()) {
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
					.getResource(String.format(STOCK_CHART_RESOURCE_FILE_PATH, stockItem.getSymbol()));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1600, 1800);
				logger.info("Finish creating chart for " + stockItem.getSymbol() + " using latest data dated: "
						+ stockItemLog.getPriceDate());
			} catch (Exception ex) {
				throw new StockException(ex);
			}
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
				showAnnotation(ftvSubplot, x, "外资买卖");
			}
			if (ttvSubplot != null) {
				mainPlot.add(ttvSubplot, 2);
				showAnnotation(ttvSubplot, x, "投信买卖");
			}
			if (mbSubplot != null) {
				mainPlot.add(mbSubplot, 2);
				showAnnotation(mbSubplot, x, "融资馀额");
			}
			if (mssSubplot != null) {
				mainPlot.add(mssSubplot, 2);
				showAnnotation(mssSubplot, x, "融券馀额");
			}
			if (sblSubplot != null) {
				mainPlot.add(sblSubplot, 2);
				showAnnotation(sblSubplot, x, "借券馀额");
			}
			if (cwtvSubplot != null) {
				mainPlot.add(cwtvSubplot, 2);
				showAnnotation(cwtvSubplot, x, "認購交易");
			}
			if (ictvSubplot != null) {
				mainPlot.add(ictvSubplot, 2);
				showAnnotation(ictvSubplot, x, "認購买卖");
			}
			//
			if (pwtvSubplot != null) {
				mainPlot.add(pwtvSubplot, 2);
				showAnnotation(pwtvSubplot, x, "認售交易");
			}
			if (iptvSubplot != null) {
				mainPlot.add(iptvSubplot, 2);
				showAnnotation(iptvSubplot, x, "認售买卖");
			}
			//
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("%s(%s)", stockItem.getName(), stockItem.getSymbol()),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();
			return chart;
		}

		private void showAnnotation(XYPlot p, double x, String text) {
			Range r = p.getRangeAxis(0).getRange();
			double y = (r.getLowerBound() + r.getUpperBound()) / 2;
			final XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
			annotation.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 24));
			
			p.addAnnotation(annotation);
		}

	}
}
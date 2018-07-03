package com.javatican.stock.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
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
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockItemDataDAO;
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.PutWarrantTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPrice;
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
	StockItemDataDAO stockItemDataDAO;
	@Autowired
	private ResourceLoader resourceLoader;

	public void createGraphs(Collection<StockItem> siList) {
		siList.stream().forEach(stockItem -> createGraph(stockItem));
	}

	public void createGraph(StockItem stockItem) {
		StockChart sc = new StockChart(stockItem);
		try {
			sc.create();
		} catch (StockException e) {
			logger.info("Cannot create chart for " + stockItem.getSymbol());
		}
	}

	public class StockChart {
		private StockItem stockItem;
		private List<StockItemData> sidList;
		private List<CallWarrantTradeSummary> cwtsList;
		private List<PutWarrantTradeSummary> pwtsList;

		public StockChart(StockItem stockItem) {
			this.stockItem = stockItem;
		}

		public void create() throws StockException {
			if (needUpdateChartForSymbol()) {
				Date latest_data_date = loadData();
				outputToPNG(StockUtils.dateToSimpleString(latest_data_date));
				// Note: calling stockItemHelper method to update chartDate property seems to
				// hit the DB a lot more than necessary.
				// such as getting a new StockItem instance and the stbt relationship items.
				// And the stockItem instance is updated twice.
				// stockItemHelper.updateChartDateForItem(stockItem, latest_data_date);
				// Note: So direct call to stockItem.setChartDate()
				stockItem.setChartDate(latest_data_date);
			} else {
				logger.info("Latest stock chart exists for " + stockItem.getSymbol());
			}
		}

		public boolean needUpdateChartForSymbol() {
			if (this.stockItem.getChartDate() == null
					|| this.stockItem.getChartDate().before(this.stockItem.getStatsDate()))
				return true;
			else
				return false;
		}

		private void outputToPNG(String dateString) throws StockException {
			JFreeChart chart = createChart();
			Resource resource = resourceLoader
					.getResource(String.format(STOCK_CHART_RESOURCE_FILE_PATH, stockItem.getSymbol()));
			try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
				ChartUtils.writeChartAsPNG(st, chart, 1920, 1080);
				logger.info("Finish creating chart for " + stockItem.getSymbol() + " for date:" + dateString);
			} catch (Exception ex) {
				throw new StockException(ex);
			}
		}

		private Date loadData() throws StockException {
			this.sidList = stockItemDataDAO.load(stockItem.getSymbol());
			this.cwtsList = callWarrantTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
			this.pwtsList = putWarrantTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
			return sidList.get(sidList.size() - 1).getTradingDate();
		}

		private JFreeChart createChart() {
			OHLCSeriesCollection priceDataset = createPriceDataset();
			TimeSeriesCollection volumeDataset = createVolumeDataset();
			TimeSeriesCollection smaDataset = createSMADataset();
			TimeSeriesCollection kdDataset = createKDDataset();

			// Create candlestick chart priceAxis
			NumberAxis priceAxis = new NumberAxis("价格");
			priceAxis.setAutoRangeIncludesZero(false);
			// Create candlestick chart renderer
			CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
					false, new HighLowItemLabelGenerator(new SimpleDateFormat("yy/MM/dd"), new DecimalFormat("0.00")));
			candlestickRenderer.setUpPaint(Color.red);
			candlestickRenderer.setDownPaint(Color.green);
			candlestickRenderer.setUseOutlinePaint(true);
			candlestickRenderer.setSeriesVisibleInLegend(0, false);
			// Create candlestickSubplot
			XYPlot candlestickSubplot = new XYPlot(priceDataset, null, priceAxis, candlestickRenderer);
			candlestickSubplot.setBackgroundPaint(Color.white);
			// create SMA renderer
			XYLineAndShapeRenderer smaRenderer = new XYLineAndShapeRenderer();
			// add 2nd dataset (SMA)
			candlestickSubplot.setDataset(1, smaDataset);
			smaRenderer.setDefaultShapesVisible(false);
			smaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
			smaRenderer.setSeriesPaint(0, Color.CYAN);
			smaRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
			smaRenderer.setSeriesPaint(1, Color.DARK_GRAY);
			smaRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
			smaRenderer.setSeriesPaint(2, Color.PINK);
			smaRenderer.setSeriesStroke(3, new BasicStroke(1.0f));
			smaRenderer.setSeriesPaint(3, Color.MAGENTA);
			candlestickSubplot.setRenderer(1, smaRenderer);
			candlestickSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			// Create volume chart volumeAxis
			NumberAxis volumeAxis = new NumberAxis("交易额");
			volumeAxis.setAutoRangeIncludesZero(true);
			// Set to no decimal
			volumeAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
			// Create volume chart renderer
			XYBarRenderer volRenderer = new XYBarRenderer();
			volRenderer.setShadowVisible(false);
			volRenderer.setSeriesVisibleInLegend(0, false);
			volRenderer.setSeriesVisibleInLegend(1, false);
			volRenderer.setSeriesVisibleInLegend(2, false);
			volRenderer.setSeriesPaint(0, Color.RED);
			volRenderer.setSeriesPaint(1, Color.GREEN);
			volRenderer.setSeriesPaint(2, Color.GRAY);
			//volRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Date={1}, Vol={2}",new SimpleDateFormat("yy/MM/dd"), new DecimalFormat("###,###")));
			// Create volumeSubplot
			XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, volRenderer);
			volumeSubplot.setBackgroundPaint(Color.white);
			//
			// Create k/d chart kdAxis
			NumberAxis kdAxis = new NumberAxis("K9/D9");
			kdAxis.setAutoRangeIncludesZero(true);
			kdAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			volumeSubplot.setRangeAxis(1, kdAxis);
			volumeSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
			volumeSubplot.setDataset(1, kdDataset );
			volumeSubplot.mapDatasetToRangeAxis(1, 1);
			// Create k/d chart renderer
			XYLineAndShapeRenderer kdRenderer = new XYLineAndShapeRenderer();
			kdRenderer.setDefaultShapesVisible(false);
			kdRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
			kdRenderer.setSeriesPaint(0, Color.BLUE);
			kdRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
			kdRenderer.setSeriesPaint(1, Color.ORANGE);
			kdRenderer.setSeriesPaint(2, Color.BLACK);
			kdRenderer.setSeriesLinesVisible(2, false);
			kdRenderer.setSeriesShapesVisible(2, true);
			kdRenderer.setSeriesShape(2, new Ellipse2D.Double(-3d, -3d, 6d, 6d));
			kdRenderer.setSeriesVisibleInLegend(2, false);
			kdRenderer.setSeriesPaint(3, Color.BLACK);
			kdRenderer.setSeriesLinesVisible(3, false);
			kdRenderer.setSeriesShapesVisible(3, true);
			kdRenderer.setSeriesShape(3, new Ellipse2D.Double(-3d, -3d, 6d, 6d));
			kdRenderer.setSeriesVisibleInLegend(3, false);
			volumeSubplot.setRenderer(1, kdRenderer);
			volumeSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			// if stockItem has Call Warrants
			XYPlot cwtvSubplot = null;
			if (!cwtsList.isEmpty()) {
				TimeSeriesCollection cwtvDataset = createCallWarrantTradeValueDataset();
				TimeSeriesCollection cwatvDataset = createCallWarrantAvgTransValueDataset();
				TimeSeriesCollection cwtDataset = createCallWarrantTransactionDataset();
				// Create call warrant trade value chart cwtvAxis
				NumberAxis cwtvAxis = new NumberAxis("交易额(認購)");
				cwtvAxis.setAutoRangeIncludesZero(true);
				// Set to no decimal
				cwtvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
				// Create call warrant trade value chart renderer
				XYBarRenderer cwtvRenderer = new XYBarRenderer();
				cwtvRenderer.setShadowVisible(false);
				cwtvRenderer.setSeriesPaint(0, Color.ORANGE);
				// Create call warrant trade value Subplot
				cwtvSubplot = new XYPlot(cwtvDataset, null, cwtvAxis, cwtvRenderer);
				cwtvSubplot.setBackgroundPaint(Color.white);
				// 2nd axis for average transaction value
				NumberAxis cwatvAxis = new NumberAxis("每筆均交易额(認購)");
				cwatvAxis.setAutoRangeIncludesZero(true);
				cwtvSubplot.setRangeAxis(1, cwatvAxis);
				cwtvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
				// 2nd dataset
				cwtvSubplot.setDataset(1, cwatvDataset);
				// map the 2nd dataset to 2nd axis
				cwtvSubplot.mapDatasetToRangeAxis(1, 1);
				// renderer for average transaction value
				XYLineAndShapeRenderer cwatvRenderer = new XYLineAndShapeRenderer();
				cwatvRenderer.setDefaultShapesVisible(false);
				cwatvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
				cwatvRenderer.setSeriesPaint(0, Color.RED);
				cwtvSubplot.setRenderer(1, cwatvRenderer);
				// 3rd axis for call warrant transaction
				NumberAxis cwtAxis = new NumberAxis("交易筆数(認購)");
				cwtAxis.setAutoRangeIncludesZero(true);
				cwtvSubplot.setRangeAxis(2, cwtAxis);
				cwtvSubplot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
				// 3rd dataset
				cwtvSubplot.setDataset(2, cwtDataset);
				// map the 3rd dataset to 3rd axis
				cwtvSubplot.mapDatasetToRangeAxis(2, 2);
				// renderer for call warrant transaction
				XYLineAndShapeRenderer cwtRenderer = new XYLineAndShapeRenderer();
				cwtRenderer.setDefaultShapesVisible(false);
				cwtRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
				cwtRenderer.setSeriesPaint(0, Color.GREEN);
				cwtvSubplot.setRenderer(2, cwtRenderer);
				cwtvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			}
			// if stockItem has Put Warrants
			XYPlot pwtvSubplot = null;
			if (!pwtsList.isEmpty()) {
				TimeSeriesCollection pwtvDataset = createPutWarrantTradeValueDataset();
				TimeSeriesCollection pwatvDataset = createPutWarrantAvgTransValueDataset();
				TimeSeriesCollection pwtDataset = createPutWarrantTransactionDataset();
				// Create put warrant trade value chart pwtvAxis
				NumberAxis pwtvAxis = new NumberAxis("交易额(認售)");
				pwtvAxis.setAutoRangeIncludesZero(true);
				// Set to no decimal
				pwtvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
				// Create put warrant trade value chart renderer
				XYBarRenderer pwtvRenderer = new XYBarRenderer();
				pwtvRenderer.setShadowVisible(false);
				pwtvRenderer.setSeriesPaint(0, Color.ORANGE);
				if(cwtvSubplot!=null) pwtvRenderer.setSeriesVisibleInLegend(0, false);
				// Create call warrant trade value Subplot
				pwtvSubplot = new XYPlot(pwtvDataset, null, pwtvAxis, pwtvRenderer);
				pwtvSubplot.setBackgroundPaint(Color.white);
				// 2nd axis for average transaction value
				NumberAxis pwatvAxis = new NumberAxis("每筆均交易额(認售)");
				pwatvAxis.setAutoRangeIncludesZero(true);
				pwtvSubplot.setRangeAxis(1, pwatvAxis);
				pwtvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
				// 2nd dataset
				pwtvSubplot.setDataset(1, pwatvDataset);
				// map the 2nd dataset to 2nd axis
				pwtvSubplot.mapDatasetToRangeAxis(1, 1);
				// renderer for average transaction value
				XYLineAndShapeRenderer pwatvRenderer = new XYLineAndShapeRenderer();
				pwatvRenderer.setDefaultShapesVisible(false);
				pwatvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
				pwatvRenderer.setSeriesPaint(0, Color.RED);
				if(cwtvSubplot!=null) pwatvRenderer.setSeriesVisibleInLegend(0, false);
				pwtvSubplot.setRenderer(1, pwatvRenderer);
				// 3rd axis for call warrant transaction
				NumberAxis pwtAxis = new NumberAxis("交易筆数(認售)");
				pwtAxis.setAutoRangeIncludesZero(true);
				pwtvSubplot.setRangeAxis(2, pwtAxis);
				pwtvSubplot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
				// 3rd dataset
				pwtvSubplot.setDataset(2, pwtDataset);
				// map the 3rd dataset to 3rd axis
				pwtvSubplot.mapDatasetToRangeAxis(2, 2);
				// renderer for call warrant transaction
				XYLineAndShapeRenderer pwtRenderer = new XYLineAndShapeRenderer();
				pwtRenderer.setDefaultShapesVisible(false);
				pwtRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
				pwtRenderer.setSeriesPaint(0, Color.GREEN);
				if(cwtvSubplot!=null) pwtRenderer.setSeriesVisibleInLegend(0, false);
				pwtvSubplot.setRenderer(2, pwtRenderer);
				pwtvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			}
			//
			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(10.0);
			mainPlot.add(candlestickSubplot, 3);
			mainPlot.add(volumeSubplot, 1);
			if (cwtvSubplot != null) {
				mainPlot.add(cwtvSubplot,1);
			}
			if (pwtvSubplot != null) {
				mainPlot.add(pwtvSubplot,1);
			}
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(String.format("%s(%s)", stockItem.getName(), stockItem.getSymbol()),
					JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();

			return chart;
		}


		private OHLCSeriesCollection createPriceDataset() {
			OHLCSeriesCollection priceDataset = new OHLCSeriesCollection();
			OHLCSeries ohlcSeries = new OHLCSeries("Price");
			// add data
			sidList.stream().forEach(sid -> ohlcSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getOpen(),
					sid.getStockPrice().getHigh(), sid.getStockPrice().getLow(), sid.getStockPrice().getClose()));
			priceDataset.addSeries(ohlcSeries);
			return priceDataset;
		}

		private TimeSeriesCollection createVolumeDataset() {
			TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
			TimeSeries pVolumeSeries = new TimeSeries("PVolume");
			TimeSeries nVolumeSeries = new TimeSeries("NVolume");
			TimeSeries zVolumeSeries = new TimeSeries("ZVolume");
			// add data
			Double oldClosePrice = 0.0;
			for(StockItemData sid: sidList) {
				 Double closePrice = sid.getStockPrice().getClose();
				 if(closePrice>oldClosePrice) {
					 pVolumeSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getTradeVolume());
				 } else if(closePrice<oldClosePrice) {
					 nVolumeSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getTradeVolume());
				 } else {
					 zVolumeSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getTradeVolume());
				 }
				 oldClosePrice = closePrice;
			}
			volumeDataset.addSeries(pVolumeSeries);
			volumeDataset.addSeries(nVolumeSeries);
			volumeDataset.addSeries(zVolumeSeries);
			return volumeDataset;
		}

		private TimeSeriesCollection createSMADataset() {
			TimeSeriesCollection smaDataset = new TimeSeriesCollection();
			TimeSeries sma60Series = new TimeSeries("SMA_60");
			TimeSeries sma20Series = new TimeSeries("SMA_20");
			TimeSeries sma10Series = new TimeSeries("SMA_10");
			TimeSeries sma5Series = new TimeSeries("SMA_5");
			// add data
			sidList.stream().forEach(sid -> {
				Date date = sid.getTradingDate();
				sma60Series.add(new Day(date), sid.getSma60());
				sma20Series.add(new Day(date), sid.getSma20());
				sma10Series.add(new Day(date), sid.getSma10());
				sma5Series.add(new Day(date), sid.getSma5());
			});
			// add data
			smaDataset.addSeries(sma5Series);
			smaDataset.addSeries(sma10Series);
			smaDataset.addSeries(sma20Series);
			smaDataset.addSeries(sma60Series);
			return smaDataset;
		}

		private TimeSeriesCollection createKDDataset() {
			TimeSeriesCollection kdDataset = new TimeSeriesCollection();
			TimeSeries kSeries = new TimeSeries("K9");
			TimeSeries dSeries = new TimeSeries("D9");
			TimeSeries ka80Series = new TimeSeries("ka80");
			TimeSeries ku20Series = new TimeSeries("ku20");
			// add data
			sidList.stream().forEach(sid -> {
				Date date = sid.getTradingDate();
				Day d = new Day(date);
				kSeries.add(d, sid.getK());
				dSeries.add(d, sid.getD());
				if(sid.getK()>=80) {
					ka80Series.add(d, sid.getK());
				}
				if(sid.getK()<=20) {
					ku20Series.add(d, sid.getK());
				}
			});
			// add data
			kdDataset.addSeries(kSeries);
			kdDataset.addSeries(dSeries);
			kdDataset.addSeries(ka80Series);
			kdDataset.addSeries(ku20Series);
			return kdDataset;
		}

		private TimeSeriesCollection createCallWarrantTradeValueDataset() {
			TimeSeriesCollection cwtvDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("交易额");
			// add data
			cwtsList.stream().forEach(cwts -> {
				series1.add(new Day(cwts.getTradingDate()), cwts.getTradeValue());
			});
			// add data
			cwtvDataset.addSeries(series1);
			return cwtvDataset;
		}

		private TimeSeriesCollection createCallWarrantAvgTransValueDataset() {
			TimeSeriesCollection cwatvDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("每筆均交易额");
			// add data
			cwtsList.stream().forEach(cwts -> {
				series1.add(new Day(cwts.getTradingDate()), cwts.getAvgTransactionValue());
			});
			// add data
			cwatvDataset.addSeries(series1);
			return cwatvDataset;
		}

		private TimeSeriesCollection createCallWarrantTransactionDataset() {
			TimeSeriesCollection cwtDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("交易筆数");
			// add data
			cwtsList.stream().forEach(cwts -> {
				series1.add(new Day(cwts.getTradingDate()), cwts.getTransaction());
			});
			// add data
			cwtDataset.addSeries(series1);
			return cwtDataset;
		}

		private TimeSeriesCollection createPutWarrantTradeValueDataset() {
			TimeSeriesCollection pwtvDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("交易额");
			// add data
			pwtsList.stream().forEach(pwts -> {
				series1.add(new Day(pwts.getTradingDate()), pwts.getTradeValue());
			});
			// add data
			pwtvDataset.addSeries(series1);
			return pwtvDataset;
		}

		private TimeSeriesCollection createPutWarrantAvgTransValueDataset() {
			TimeSeriesCollection pwatvDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("每筆均交易额");
			// add data
			pwtsList.stream().forEach(pwts -> {
				series1.add(new Day(pwts.getTradingDate()), pwts.getAvgTransactionValue());
			});
			// add data
			pwatvDataset.addSeries(series1);
			return pwatvDataset;
		}

		private TimeSeriesCollection createPutWarrantTransactionDataset() {
			TimeSeriesCollection pwtDataset = new TimeSeriesCollection();
			TimeSeries series1 = new TimeSeries("交易筆数");
			// add data
			pwtsList.stream().forEach(pwts -> {
				series1.add(new Day(pwts.getTradingDate()), pwts.getTransaction());
			});
			// add data
			pwtDataset.addSeries(series1);
			return pwtDataset;
		}

	}
}
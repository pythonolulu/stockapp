package com.javatican.stock.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.CallWarrantTradeSummaryDAO;
import com.javatican.stock.dao.StockTradeByForeignDAO;
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockTradeByForeign;

@Component("ftvPlot")
public class ForeignTradeVolumePlot implements JPlot {

	@Autowired
	StockTradeByForeignDAO stockTradeByForeignDAO;
	private List<StockTradeByForeign> stbfList;

	public ForeignTradeVolumePlot() {
	}

	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.stbfList = stockTradeByForeignDAO.getByStockSymbol(stockItem.getSymbol());
		if (stbfList.isEmpty())
			return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection ftvDataset = createForeignTradeVolumeDataset();
		TimeSeriesCollection ftnvDataset = createForeignTradeNetVolumeDataset();
		TimeSeriesCollection fabvDataset = createForeignAccumulateBuyVolumeDataset();
		// Create foreign trade volume chart ftvAxis
		NumberAxis ftvAxis = new NumberAxis("买卖量(千股)");
		ftvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ftvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create foreign trade volume chart renderer
		XYBarRenderer ftvRenderer = new XYBarRenderer();
		ftvRenderer.setShadowVisible(false);
		ftvRenderer.setSeriesPaint(0, Color.ORANGE);
		ftvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		// Create foreign trade volume Subplot
		XYPlot ftvSubplot = new XYPlot(ftvDataset, null, ftvAxis, ftvRenderer);
		ftvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		ftvSubplot.setDataset(1, ftnvDataset);
		//
		XYLineAndShapeRenderer ftnvRenderer = new XYLineAndShapeRenderer();
		ftnvRenderer.setSeriesPaint(0, Color.RED);
		ftnvRenderer.setSeriesLinesVisible(0, false);
		ftnvRenderer.setSeriesShapesVisible(0, true);
		ftnvRenderer.setSeriesShape(0, new Ellipse2D.Double(-2d, -2d, 4d, 4d));
		//
		ftnvRenderer.setSeriesPaint(1, Color.BLUE);
		ftnvRenderer.setSeriesLinesVisible(1, false);
		ftnvRenderer.setSeriesShapesVisible(1, true);
		Shape tri = ShapeUtils.createDownTriangle(1.5F);
		ftnvRenderer.setSeriesShape(1, tri);
		ftvSubplot.setRenderer(1, ftnvRenderer);
		// 2nd axis
		NumberAxis fabvAxis = new NumberAxis("外资累积净买(千股)");
		fabvAxis.setAutoRangeIncludesZero(true);
		ftvSubplot.setRangeAxis(1, fabvAxis);
		ftvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		ftvSubplot.setDataset(2, fabvDataset);
		// map the 3rd dataset to 2nd axis
		ftvSubplot.mapDatasetToRangeAxis(2, 1);
		//3rd renderer
		XYLineAndShapeRenderer fabvRenderer = new XYLineAndShapeRenderer();
		fabvRenderer.setDefaultShapesVisible(false);
		fabvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		fabvRenderer.setSeriesPaint(0, Color.MAGENTA);
		ftvSubplot.setRenderer(2, fabvRenderer);
		ftvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return ftvSubplot;
	}

	private TimeSeriesCollection createForeignTradeVolumeDataset() {
		TimeSeriesCollection ftvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("外资买");
		TimeSeries series2 = new TimeSeries("外资卖");
		// add data
		stbfList.stream().forEach(stbf -> {
			series1.add(new Day(stbf.getTradingDate()), stbf.getBuy() / 1000);
			series2.add(new Day(stbf.getTradingDate()), -1 * stbf.getSell() / 1000);
		});
		// add data
		ftvDataset.addSeries(series1);
		ftvDataset.addSeries(series2);
		return ftvDataset;
	}

	private TimeSeriesCollection createForeignTradeNetVolumeDataset() {
		TimeSeriesCollection ftnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("外资净买");
		TimeSeries series2 = new TimeSeries("外资净卖");
		// add data
		stbfList.stream().forEach(stbf -> {
			double amount = stbf.getDiff();
			if (amount > 0) {
				series1.add(new Day(stbf.getTradingDate()), amount / 1000);
			} else {
				series2.add(new Day(stbf.getTradingDate()), amount / 1000);
			}
		});
		// add data
		ftnvDataset.addSeries(series1);
		ftnvDataset.addSeries(series2);
		return ftnvDataset;
	}

	private TimeSeriesCollection createForeignAccumulateBuyVolumeDataset() {
		TimeSeriesCollection fabvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("外资累积净买");
		// add data
		double prevSum = 0.0;
		for (StockTradeByForeign stbf : stbfList) {
			double newSum = prevSum + stbf.getDiff() / 1000;
			series1.add(new Day(stbf.getTradingDate()), newSum);
			prevSum = newSum;
		}
		// add data
		fabvDataset.addSeries(series1);
		return fabvDataset;
	}

}

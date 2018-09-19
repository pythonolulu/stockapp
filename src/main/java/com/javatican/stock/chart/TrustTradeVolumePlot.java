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
import com.javatican.stock.dao.StockTradeByTrustDAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.util.StockChartUtils;

@Component("ttvPlot")
public class TrustTradeVolumePlot implements JPlot {

	@Autowired
	StockTradeByTrustDAO stockTradeByTrustDAO;
	private List<StockTradeByTrust> stbtList;

	public TrustTradeVolumePlot() {
	}

	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.stbtList = stockTradeByTrustDAO.getByStockSymbol(stockItem.getSymbol());
		if (stbtList.isEmpty())
			return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection ttvDataset = createTrustTradeVolumeDataset();
		TimeSeriesCollection ttnvDataset = createTrustTradeNetVolumeDataset();
		TimeSeriesCollection tabvDataset = createTrustAccumulateBuyVolumeDataset();
		// Create trust trade volume chart ftvAxis
		NumberAxis ttvAxis = new NumberAxis("买卖量(千股)");
		ttvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ttvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create trust trade volume chart renderer
		XYBarRenderer ttvRenderer = new XYBarRenderer();
		ttvRenderer.setShadowVisible(false);
		ttvRenderer.setSeriesPaint(0, Color.ORANGE);
		ttvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		// Create trust trade volume Subplot
		XYPlot ttvSubplot = new XYPlot(ttvDataset, null, ttvAxis, ttvRenderer);
		ttvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		ttvSubplot.setDataset(1, ttnvDataset);
		//
		XYLineAndShapeRenderer ttnvRenderer = new XYLineAndShapeRenderer();
		ttnvRenderer.setSeriesPaint(0, Color.RED);
		ttnvRenderer.setSeriesLinesVisible(0, false);
		ttnvRenderer.setSeriesShapesVisible(0, true);
		ttnvRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		//
		ttnvRenderer.setSeriesPaint(1, Color.BLUE);
		ttnvRenderer.setSeriesLinesVisible(1, false);
		ttnvRenderer.setSeriesShapesVisible(1, true);
		ttnvRenderer.setSeriesShape(1, StockChartUtils.getDownTriangleShape());
		ttvSubplot.setRenderer(1, ttnvRenderer);
		// 2nd axis
		NumberAxis tabvAxis = new NumberAxis("投信累积净买(千股)");
		tabvAxis.setAutoRangeIncludesZero(true);
		ttvSubplot.setRangeAxis(1, tabvAxis);
		ttvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		ttvSubplot.setDataset(2, tabvDataset);
		// map the 3rd dataset to 2nd axis
		ttvSubplot.mapDatasetToRangeAxis(2, 1);
		//3rd renderer
		XYLineAndShapeRenderer tabvRenderer = new XYLineAndShapeRenderer();
		tabvRenderer.setDefaultShapesVisible(false);
		tabvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		tabvRenderer.setSeriesPaint(0, Color.MAGENTA);
		ttvSubplot.setRenderer(2, tabvRenderer);
		ttvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return ttvSubplot;
	}

	private TimeSeriesCollection createTrustTradeVolumeDataset() {
		TimeSeriesCollection ttvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("投信买");
		TimeSeries series2 = new TimeSeries("投信卖");
		// add data
		stbtList.stream().forEach(stbt -> {
			series1.add(new Day(stbt.getTradingDate()), stbt.getBuy() / 1000);
			series2.add(new Day(stbt.getTradingDate()), -1 * stbt.getSell() / 1000);
		});
		// add data
		ttvDataset.addSeries(series1);
		ttvDataset.addSeries(series2);
		return ttvDataset;
	}

	private TimeSeriesCollection createTrustTradeNetVolumeDataset() {
		TimeSeriesCollection ttnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("投信净买");
		TimeSeries series2 = new TimeSeries("投信净卖");
		// add data
		stbtList.stream().forEach(stbt -> {
			double amount = stbt.getDiff();
			if (amount > 0) {
				series1.add(new Day(stbt.getTradingDate()), amount / 1000);
			} else {
				series2.add(new Day(stbt.getTradingDate()), amount / 1000);
			}
		});
		// add data
		ttnvDataset.addSeries(series1);
		ttnvDataset.addSeries(series2);
		return ttnvDataset;
	}

	private TimeSeriesCollection createTrustAccumulateBuyVolumeDataset() {
		TimeSeriesCollection tabvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("投信累积净买");
		// add data
		double prevSum = 0.0;
		for (StockTradeByTrust stbt : stbtList) {
			double newSum = prevSum + stbt.getDiff() / 1000;
			series1.add(new Day(stbt.getTradingDate()), newSum);
			prevSum = newSum;
		}
		// add data
		tabvDataset.addSeries(series1);
		return tabvDataset;
	}

}

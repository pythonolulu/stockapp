package com.javatican.stock.index.chart;

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
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.util.StockChartUtils;

@Component("iftvPlot")
public class IndexForeignTradeVolumePlot {

	@Autowired
	TradingValueDAO tradingValueDAO;
 
	private List<TradingValue> tvList;

	 
	public IndexForeignTradeVolumePlot() {
	}

	public Plot getPlot() throws StockException {
		tvList = tradingValueDAO.findAll();
		return createPlot();
	} 

	private XYPlot createPlot() {
		TimeSeriesCollection ftvDataset = createForeignTradeVolumeDataset();
		TimeSeriesCollection ftnvDataset = createForeignTradeNetVolumeDataset();
		TimeSeriesCollection fabvDataset = createForeignAccumulateBuyVolumeDataset();
		// Create foreign trade volume chart ftvAxis
		NumberAxis ftvAxis = new NumberAxis("买卖量(10亿)");
		ftvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ftvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create foreign trade volume chart renderer
		XYBarRenderer ftvRenderer = new XYBarRenderer();
		ftvRenderer.setDefaultSeriesVisibleInLegend(true);
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
		ftnvRenderer.setDefaultSeriesVisibleInLegend(true);
		ftnvRenderer.setSeriesPaint(0, Color.RED);
		ftnvRenderer.setSeriesLinesVisible(0, false);
		ftnvRenderer.setSeriesShapesVisible(0, true);
		ftnvRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		//
		ftnvRenderer.setSeriesPaint(1, Color.BLUE);
		ftnvRenderer.setSeriesLinesVisible(1, false);
		ftnvRenderer.setSeriesShapesVisible(1, true);
		ftnvRenderer.setSeriesShape(1, StockChartUtils.getDownTriangleShape());
		ftvSubplot.setRenderer(1, ftnvRenderer);
		// 2nd axis
		NumberAxis fabvAxis = new NumberAxis("外资累积净买(10亿)");
		fabvAxis.setAutoRangeIncludesZero(true);
		ftvSubplot.setRangeAxis(1, fabvAxis);
		ftvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		ftvSubplot.setDataset(2, fabvDataset);
		// map the 3rd dataset to 2nd axis
		ftvSubplot.mapDatasetToRangeAxis(2, 1);
		//3rd renderer
		XYLineAndShapeRenderer fabvRenderer = new XYLineAndShapeRenderer();
		fabvRenderer.setDefaultSeriesVisibleInLegend(true);
		fabvRenderer.setDefaultShapesVisible(false);
		fabvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		fabvRenderer.setSeriesPaint(0, Color.BLACK);
		ftvSubplot.setRenderer(2, fabvRenderer);
		ftvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return ftvSubplot;
	}

	private TimeSeriesCollection createForeignTradeVolumeDataset() {
		TimeSeriesCollection ftvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("买");
		TimeSeries series2 = new TimeSeries("卖");
		// add data
		tvList.stream().forEach(tv -> {
			series1.add(new Day(tv.getTradingDate()), tv.getForeignBuy() / 1000000000);
			series2.add(new Day(tv.getTradingDate()), -1 * tv.getForeignSell() / 1000000000);
		});
		// add data
		ftvDataset.addSeries(series1);
		ftvDataset.addSeries(series2);
		return ftvDataset;
	}

	private TimeSeriesCollection createForeignTradeNetVolumeDataset() {
		TimeSeriesCollection ftnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("净买");
		TimeSeries series2 = new TimeSeries("净卖");
		// add data
		tvList.stream().forEach(tv -> {
			double amount = tv.getForeignDiff();
			if (amount > 0) {
				series1.add(new Day(tv.getTradingDate()), amount / 1000000000);
			} else {
				series2.add(new Day(tv.getTradingDate()), amount / 1000000000);
			}
		});
		// add data
		ftnvDataset.addSeries(series1);
		ftnvDataset.addSeries(series2);
		return ftnvDataset;
	}

	private TimeSeriesCollection createForeignAccumulateBuyVolumeDataset() {
		TimeSeriesCollection fabvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("累积净买");
		// add data
		double prevSum = 0.0;
		for (TradingValue tv : tvList) {
			double newSum = prevSum + tv.getForeignDiff() / 1000000000;
			series1.add(new Day(tv.getTradingDate()), newSum);
			prevSum = newSum;
		}
		// add data
		fabvDataset.addSeries(series1);
		return fabvDataset;
	}

}

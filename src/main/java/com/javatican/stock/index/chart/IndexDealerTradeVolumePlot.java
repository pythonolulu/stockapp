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

@Component("idtvPlot")
public class IndexDealerTradeVolumePlot {

	@Autowired
	TradingValueDAO tradingValueDAO;

	private List<TradingValue> tvList;

	public IndexDealerTradeVolumePlot() {
	}

	public Plot getPlot() throws StockException {
		tvList = tradingValueDAO.findAll();
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection dtvDataset = createDealerTradeVolumeDataset();
		TimeSeriesCollection dtnvDataset = createDealerTradeNetVolumeDataset();
		TimeSeriesCollection dabvDataset = createDealerAccumulateBuyVolumeDataset();
		// Create trust trade volume chart ftvAxis
		NumberAxis dtvAxis = new NumberAxis("买卖量(10亿)");
		dtvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		dtvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create trust trade volume chart renderer
		XYBarRenderer dtvRenderer = new XYBarRenderer();
		dtvRenderer.setShadowVisible(false);
		dtvRenderer.setSeriesPaint(0, Color.ORANGE);
		dtvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		// Create trust trade volume Subplot
		XYPlot dtvSubplot = new XYPlot(dtvDataset, null, dtvAxis, dtvRenderer);
		dtvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		dtvSubplot.setDataset(1, dtnvDataset);
		//
		XYLineAndShapeRenderer dtnvRenderer = new XYLineAndShapeRenderer();
		dtnvRenderer.setSeriesPaint(0, Color.RED);
		dtnvRenderer.setSeriesLinesVisible(0, false);
		dtnvRenderer.setSeriesShapesVisible(0, true);
		dtnvRenderer.setSeriesShape(0, new Ellipse2D.Double(-2d, -2d, 4d, 4d));
		//
		dtnvRenderer.setSeriesPaint(1, Color.BLUE);
		dtnvRenderer.setSeriesLinesVisible(1, false);
		dtnvRenderer.setSeriesShapesVisible(1, true);
		Shape tri = ShapeUtils.createDownTriangle(1.5F);
		dtnvRenderer.setSeriesShape(1, tri);
		dtvSubplot.setRenderer(1, dtnvRenderer);
		// 2nd axis
		NumberAxis dabvAxis = new NumberAxis("自营商累积净买(10亿)");
		dabvAxis.setAutoRangeIncludesZero(true);
		dtvSubplot.setRangeAxis(1, dabvAxis);
		dtvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		dtvSubplot.setDataset(2, dabvDataset);
		// map the 3rd dataset to 2nd axis
		dtvSubplot.mapDatasetToRangeAxis(2, 1);
		// 3rd renderer
		XYLineAndShapeRenderer dabvRenderer = new XYLineAndShapeRenderer();
		dabvRenderer.setDefaultShapesVisible(false);
		dabvRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		dabvRenderer.setSeriesPaint(0, Color.MAGENTA);
		dtvSubplot.setRenderer(2, dabvRenderer);
		dtvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return dtvSubplot;
	}

	private TimeSeriesCollection createDealerTradeVolumeDataset() {
		TimeSeriesCollection dtvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("自营商买");
		TimeSeries series2 = new TimeSeries("自营商卖");
		// add data
		tvList.stream().forEach(tv -> {
			series1.add(new Day(tv.getTradingDate()), tv.getDealerBuy() / 1000000000);
			series2.add(new Day(tv.getTradingDate()), -1 * tv.getDealerSell() / 1000000000);
		});
		// add data
		dtvDataset.addSeries(series1);
		dtvDataset.addSeries(series2);
		return dtvDataset;
	}

	private TimeSeriesCollection createDealerTradeNetVolumeDataset() {
		TimeSeriesCollection dtnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("自营商净买");
		TimeSeries series2 = new TimeSeries("自营商净卖");
		// add data
		tvList.stream().forEach(tv -> {
			double amount = tv.getDealerDiff();
			if (amount > 0) {
				series1.add(new Day(tv.getTradingDate()), amount / 1000000000);
			} else {
				series2.add(new Day(tv.getTradingDate()), amount / 1000000000);
			}
		});
		// add data
		dtnvDataset.addSeries(series1);
		dtnvDataset.addSeries(series2);
		return dtnvDataset;
	}

	private TimeSeriesCollection createDealerAccumulateBuyVolumeDataset() {
		TimeSeriesCollection dabvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("自营商累积净买");
		// add data
		double prevSum = 0.0;
		for (TradingValue tv : tvList) {
			double newSum = prevSum + tv.getDealerDiff() / 1000000000;
			series1.add(new Day(tv.getTradingDate()), newSum);
			prevSum = newSum;
		}
		// add data
		dabvDataset.addSeries(series1);
		return dabvDataset;
	}

}

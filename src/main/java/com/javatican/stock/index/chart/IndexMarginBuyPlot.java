package com.javatican.stock.index.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
//import java.awt.geom.Ellipse2D;
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

@Component("imbPlot")
public class IndexMarginBuyPlot {

	@Autowired
	TradingValueDAO tradingValueDAO;
 
	private List<TradingValue> tvList;

	public IndexMarginBuyPlot() {
	}
	
	public Plot getPlot() throws StockException {
		tvList = tradingValueDAO.findAll();
		return createPlot();
	} 
	
	private XYPlot createPlot() {
		TimeSeriesCollection mbDataset = this.createMarginBuyDataset();
		TimeSeriesCollection mbnDataset = this.createMarginBuyNetDataset();
		TimeSeriesCollection mbaDataset = this.createMaringBuyAccuDataset();
		//
		NumberAxis mbAxis = new NumberAxis("买卖量(千股)");
		mbAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		mbAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer mbRenderer = new XYBarRenderer();
		mbRenderer.setShadowVisible(false);
		mbRenderer.setSeriesPaint(0, Color.BLUE);
		mbRenderer.setSeriesPaint(1, Color.YELLOW);
		//
		XYPlot mbSubplot = new XYPlot(mbDataset, null, mbAxis, mbRenderer);
		mbSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		mbSubplot.setDataset(1, mbnDataset);
		//
		XYLineAndShapeRenderer mbnRenderer = new XYLineAndShapeRenderer();
		mbnRenderer.setSeriesPaint(0, Color.RED);
		mbnRenderer.setSeriesLinesVisible(0, false);
		mbnRenderer.setSeriesShapesVisible(0, true);
		Shape tri = ShapeUtils.createUpTriangle(1.5F);
		mbnRenderer.setSeriesShape(0, tri);
		//
		mbnRenderer.setSeriesPaint(1, Color.GRAY);
		mbnRenderer.setSeriesLinesVisible(1, false);
		mbnRenderer.setSeriesShapesVisible(1, true);
		tri = ShapeUtils.createDownTriangle(1.5F);
		mbnRenderer.setSeriesShape(1, tri);
		mbSubplot.setRenderer(1, mbnRenderer);
		// 2nd axis
		NumberAxis mbaAxis = new NumberAxis("融资馀额(千股)");
		mbaAxis.setAutoRangeIncludesZero(false);
		mbSubplot.setRangeAxis(1, mbaAxis);
		mbSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		mbSubplot.setDataset(2,mbaDataset);
		// map the 3rd dataset to 2nd axis
		mbSubplot.mapDatasetToRangeAxis(2, 1);
		//
		XYLineAndShapeRenderer mbaRenderer = new XYLineAndShapeRenderer();
		mbaRenderer.setDefaultShapesVisible(false);
		mbaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		mbaRenderer.setSeriesPaint(0, Color.GREEN);
		mbSubplot.setRenderer(2, mbaRenderer);
		mbSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return mbSubplot;
	}


	private TimeSeriesCollection createMarginBuyDataset() {
		TimeSeriesCollection mbDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融资买");
		TimeSeries series2 = new TimeSeries("融资卖");
		// add data 
		tvList.stream().forEach(tv -> {
			series1.add(new Day(tv.getTradingDate()), tv.getMarginBuy());
			series2.add(new Day(tv.getTradingDate()), -1 * tv.getMarginRedemp());
		});
		// add data
		mbDataset.addSeries(series1);
		mbDataset.addSeries(series2);
		return mbDataset;
	}

	private TimeSeriesCollection createMarginBuyNetDataset() {
		TimeSeriesCollection mbnDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融资净买");
		TimeSeries series2 = new TimeSeries("融资净卖");
		// add data
		tvList.stream().forEach(tv -> {
			double amount = tv.getMarginBuy() - tv.getMarginRedemp();
			if (amount > 0) {
				series1.add(new Day(tv.getTradingDate()), amount);
			} else {
				series2.add(new Day(tv.getTradingDate()), amount);
			}
		});
		// add data
		mbnDataset.addSeries(series1);
		mbnDataset.addSeries(series2);
		return mbnDataset;
	}

	private TimeSeriesCollection createMaringBuyAccuDataset() {
		TimeSeriesCollection mbaDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融资馀额");
		// add data
		tvList.stream().forEach(tv -> {
			series1.add(new Day(tv.getTradingDate()), tv.getMarginAcc());
		});
		// add data
		mbaDataset.addSeries(series1);
		return mbaDataset;
	}
}

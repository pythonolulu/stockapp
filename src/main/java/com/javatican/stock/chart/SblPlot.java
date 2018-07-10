package com.javatican.stock.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException; 
import com.javatican.stock.dao.DealerTradeSummaryDAO;
import com.javatican.stock.dao.MarginSblWithDateDAO;
import com.javatican.stock.model.DealerTradeSummary;
import com.javatican.stock.model.MarginSblWithDate;
import com.javatican.stock.model.StockItem;

@Component("sblPlot")
public class SblPlot implements JPlot {

	@Autowired
	MarginSblWithDateDAO marginSblWithDateDAO;
	private List<MarginSblWithDate> mswdList;

	public SblPlot() {
	}
	
	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.mswdList = marginSblWithDateDAO.load(stockItem.getSymbol());
		if(mswdList.isEmpty()) return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection sblDataset = this.createSblDataset();
		TimeSeriesCollection sblnDataset = this.createSblNetDataset();
		TimeSeriesCollection sblaDataset = this.createSblAccuDataset();
		//
		NumberAxis sblAxis = new NumberAxis("卖买量(千股)");
		sblAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		sblAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer sblRenderer = new XYBarRenderer();
		sblRenderer.setShadowVisible(false);
		sblRenderer.setSeriesPaint(0, Color.BLUE);
		sblRenderer.setSeriesPaint(1, Color.YELLOW);
		//
		XYPlot sblSubplot = new XYPlot(sblDataset, null, sblAxis, sblRenderer);
		sblSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		sblSubplot.setDataset(1, sblnDataset);
		//
		XYLineAndShapeRenderer sblnRenderer = new XYLineAndShapeRenderer();
		sblnRenderer.setSeriesPaint(0, Color.RED);
		sblnRenderer.setSeriesLinesVisible(0, false);
		sblnRenderer.setSeriesShapesVisible(0, true);
		Shape tri = ShapeUtils.createUpTriangle(1.5F);
		sblnRenderer.setSeriesShape(0, tri);
		//
		sblnRenderer.setSeriesPaint(1, Color.GRAY);
		sblnRenderer.setSeriesLinesVisible(1, false);
		sblnRenderer.setSeriesShapesVisible(1, true);
		tri = ShapeUtils.createDownTriangle(1.5F);
		sblnRenderer.setSeriesShape(1, tri);
		sblSubplot.setRenderer(1, sblnRenderer);
		// 2nd axis
		NumberAxis sblaAxis = new NumberAxis("借券馀额(千股)");
		sblaAxis.setAutoRangeIncludesZero(true);
		sblSubplot.setRangeAxis(1, sblaAxis);
		sblSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		sblSubplot.setDataset(2,sblaDataset);
		// map the 3rd dataset to 2nd axis
		sblSubplot.mapDatasetToRangeAxis(2, 1);
		//
		XYLineAndShapeRenderer sblaRenderer = new XYLineAndShapeRenderer();
		sblaRenderer.setDefaultShapesVisible(false);
		sblaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		sblaRenderer.setSeriesPaint(0, Color.GREEN);
		sblSubplot.setRenderer(2, sblaRenderer);
		sblSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		//
		return sblSubplot;
	}


	private TimeSeriesCollection createSblDataset() {
		TimeSeriesCollection sblDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("借券卖");
		TimeSeries series2 = new TimeSeries("借券买");
		// add data
		mswdList.stream().forEach(mswd -> {
			series1.add(new Day(mswd.getTradingDate()), mswd.getSbl());
			series2.add(new Day(mswd.getTradingDate()), -1 * mswd.getSblRedemp());
		});
		// add data
		sblDataset.addSeries(series1);
		sblDataset.addSeries(series2);
		return sblDataset;
	}

	private TimeSeriesCollection createSblNetDataset() {
		TimeSeriesCollection sblnDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("借券净卖");
		TimeSeries series2 = new TimeSeries("借券净买");
		// add data
		mswdList.stream().forEach(mswd -> {
			double amount = mswd.getSbl() - mswd.getSblRedemp();
			if (amount > 0) {
				series1.add(new Day(mswd.getTradingDate()), amount);
			} else {
				series2.add(new Day(mswd.getTradingDate()), amount);
			}
		});
		// add data
		sblnDataset.addSeries(series1);
		sblnDataset.addSeries(series2);
		return sblnDataset;
	}

	private TimeSeriesCollection createSblAccuDataset() {
		TimeSeriesCollection sblaDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("借券馀额");
		// add data
		mswdList.stream().forEach(mswd -> {
			series1.add(new Day(mswd.getTradingDate()), mswd.getSblAcc());
		});
		// add data
		sblaDataset.addSeries(series1);
		return sblaDataset;
	}
}

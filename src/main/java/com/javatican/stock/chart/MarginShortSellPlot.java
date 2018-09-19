package com.javatican.stock.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
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
import com.javatican.stock.dao.MarginSblWithDateDAO;
import com.javatican.stock.model.MarginSblWithDate;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockChartUtils;

@Component("mssPlot")
public class MarginShortSellPlot implements JPlot {

	@Autowired
	MarginSblWithDateDAO marginSblWithDateDAO;
	private List<MarginSblWithDate> mswdList;

	public MarginShortSellPlot() {
	}
	
	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.mswdList = marginSblWithDateDAO.load(stockItem.getSymbol());
		if(mswdList.isEmpty()) return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection mssDataset = this.createMarginShortSellDataset();
		TimeSeriesCollection mssnDataset = this.createMarginShortSellNetDataset();
		TimeSeriesCollection mssaDataset = this.createMaringShortSellAccuDataset();
		//
		NumberAxis mssAxis = new NumberAxis("卖买量(千股)");
		mssAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		mssAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer mssRenderer = new XYBarRenderer();
		mssRenderer.setShadowVisible(false);
		mssRenderer.setSeriesPaint(0, Color.BLUE);
		mssRenderer.setSeriesPaint(1, Color.YELLOW);
		//
		XYPlot mssSubplot = new XYPlot(mssDataset, null, mssAxis, mssRenderer);
		mssSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		mssSubplot.setDataset(1, mssnDataset);
		//
		XYLineAndShapeRenderer mssnRenderer = new XYLineAndShapeRenderer();
		mssnRenderer.setSeriesPaint(0, Color.RED);
		mssnRenderer.setSeriesLinesVisible(0, false);
		mssnRenderer.setSeriesShapesVisible(0, true);
		mssnRenderer.setSeriesShape(0, StockChartUtils.getUpTriangleShape());
		//
		mssnRenderer.setSeriesPaint(1, Color.GRAY);
		mssnRenderer.setSeriesLinesVisible(1, false);
		mssnRenderer.setSeriesShapesVisible(1, true);
		mssnRenderer.setSeriesShape(1, StockChartUtils.getDownTriangleShape());
		mssSubplot.setRenderer(1, mssnRenderer);
		// 2nd axis
		NumberAxis mssaAxis = new NumberAxis("融券馀额(千股)");
		mssaAxis.setAutoRangeIncludesZero(true);
		mssSubplot.setRangeAxis(1, mssaAxis);
		mssSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		mssSubplot.setDataset(2,mssaDataset);
		// map the 3rd dataset to 2nd axis
		mssSubplot.mapDatasetToRangeAxis(2, 1);
		//
		XYLineAndShapeRenderer mssaRenderer = new XYLineAndShapeRenderer();
		mssaRenderer.setDefaultShapesVisible(false);
		mssaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		mssaRenderer.setSeriesPaint(0, Color.GREEN);
		mssSubplot.setRenderer(2, mssaRenderer);
		mssSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return mssSubplot;
	}


	private TimeSeriesCollection createMarginShortSellDataset() {
		TimeSeriesCollection mssDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融券卖");
		TimeSeries series2 = new TimeSeries("融券买");
		// add data
		mswdList.stream().forEach(mswd -> {
			series1.add(new Day(mswd.getTradingDate()), mswd.getShortSell());
			series2.add(new Day(mswd.getTradingDate()), -1 * mswd.getShortRedemp());
		});
		// add data
		mssDataset.addSeries(series1);
		mssDataset.addSeries(series2);
		return mssDataset;
	}

	private TimeSeriesCollection createMarginShortSellNetDataset() {
		TimeSeriesCollection mssnDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融资净卖");
		TimeSeries series2 = new TimeSeries("融资净买");
		// add data
		mswdList.stream().forEach(mswd -> {
			double amount = mswd.getShortSell() - mswd.getShortRedemp();
			if (amount > 0) {
				series1.add(new Day(mswd.getTradingDate()), amount);
			} else {
				series2.add(new Day(mswd.getTradingDate()), amount);
			}
		});
		// add data
		mssnDataset.addSeries(series1);
		mssnDataset.addSeries(series2);
		return mssnDataset;
	}

	private TimeSeriesCollection createMaringShortSellAccuDataset() {
		TimeSeriesCollection mssaDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("融券馀额");
		// add data
		mswdList.stream().forEach(mswd -> {
			series1.add(new Day(mswd.getTradingDate()), mswd.getShortAcc());
		});
		// add data
		mssaDataset.addSeries(series1);
		return mssaDataset;
	}
}

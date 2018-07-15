package com.javatican.stock.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.PutWarrantSelectStrategy1DAO;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockUtils;

@Component
public abstract class PutWarrantSelectStrategy1Plot implements JPlot {

	@Autowired
	PutWarrantSelectStrategy1DAO putWarrantSelectStrategy1DAO;

	private Map<String, Double> upPercentMap;

	public PutWarrantSelectStrategy1Plot() {
	}

	public abstract int getHoldPeriod();

	@Override
	public Plot getPlot(StockItem stockItem) {
		try {
			this.upPercentMap = putWarrantSelectStrategy1DAO.load(stockItem.getSymbol(), getHoldPeriod());
			if (upPercentMap.isEmpty())
				return null;
			return createPlot();
		} catch (StockException e) {
			return null;
		}
	}

	private XYPlot createPlot() {
		TimeSeriesCollection upDataset = this.createUpPercentageDataset();
		TimeSeriesCollection upaDataset = this.createUpPercentageAccDataset();
		//
		NumberAxis upAxis = new NumberAxis("涨跌(百万比)");
		upAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		upAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer upRenderer = new XYBarRenderer();
		upRenderer.setShadowVisible(false);
		upRenderer.setSeriesPaint(0, Color.CYAN);
		upRenderer.setSeriesPaint(1, Color.PINK);
		//
		XYPlot upSubplot = new XYPlot(upDataset, null, upAxis, upRenderer);
		upSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		NumberAxis upaAxis = new NumberAxis("累计涨跌幅(百万比)");
		upaAxis.setAutoRangeIncludesZero(true);
		upSubplot.setRangeAxis(1, upaAxis);
		upSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		//
		upSubplot.setDataset(1, upaDataset);
		// map the 3rd dataset to 3rd axis
		upSubplot.mapDatasetToRangeAxis(1, 1);
		//
		XYLineAndShapeRenderer upaRenderer = new XYLineAndShapeRenderer();
		upaRenderer.setDefaultShapesVisible(false);
		upaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		upaRenderer.setSeriesPaint(0, Color.GRAY);
		upSubplot.setRenderer(1, upaRenderer);
		upSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return upSubplot;
	}

	private TimeSeriesCollection createUpPercentageDataset() {
		TimeSeriesCollection upDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("涨(百分比)");
		TimeSeries series2 = new TimeSeries("跌(百分比)");
		// add data
		upPercentMap.entrySet().stream().forEach(e -> {
			Day d = new Day(StockUtils.stringSimpleToDate(e.getKey()).get());
			if (e.getValue() > 0) {
				series1.add(d, e.getValue() * 100);
			} else {
				series2.add(d, e.getValue() * 100);
			}
		});
		// add data
		upDataset.addSeries(series1);
		upDataset.addSeries(series2);
		return upDataset;
	}

	private TimeSeriesCollection createUpPercentageAccDataset() {
		TimeSeriesCollection upaDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("累计涨跌幅(百分比)");
		// add data
		double prevSum = 0.0;
		for (Map.Entry<String, Double> e : upPercentMap.entrySet()) {
			Day d = new Day(StockUtils.stringSimpleToDate(e.getKey()).get());
			double newSum = prevSum + e.getValue();
			series1.add(d, newSum * 100);
			prevSum = newSum;
		}
		// add data
		upaDataset.addSeries(series1);
		return upaDataset;
	}
}

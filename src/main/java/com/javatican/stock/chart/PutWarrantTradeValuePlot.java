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
import com.javatican.stock.dao.PutWarrantTradeSummaryDAO;
import com.javatican.stock.model.PutWarrantTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockChartUtils;

@Component("pwtvPlot")
public class PutWarrantTradeValuePlot implements JPlot {

	@Autowired
	PutWarrantTradeSummaryDAO putWarrantTradeSummaryDAO;
	private List<PutWarrantTradeSummary> pwtsList;

	public PutWarrantTradeValuePlot() {
	}

	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.pwtsList = putWarrantTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
		if (pwtsList.isEmpty())
			return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection pwtvDataset = createPutWarrantTradeValueDataset();
		TimeSeriesCollection pwatvDataset = createPutWarrantAvgTransValueDataset();
		TimeSeriesCollection pwtDataset = createPutWarrantTransactionDataset();
		// Create put warrant trade value chart pwtvAxis
		NumberAxis pwtvAxis = new NumberAxis("交易额-認售(百万)");
		pwtvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		pwtvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create put warrant trade value chart renderer
		XYBarRenderer pwtvRenderer = new XYBarRenderer();
		pwtvRenderer.setShadowVisible(false);
		pwtvRenderer.setSeriesPaint(0, Color.GRAY);
		// Create call warrant trade value Subplot
		XYPlot pwtvSubplot = new XYPlot(pwtvDataset, null, pwtvAxis, pwtvRenderer);
		pwtvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd axis for average transaction value
		NumberAxis pwatvAxis = new NumberAxis("每筆均交易额-認售(千)");
		pwatvAxis.setAutoRangeIncludesZero(true);
		pwtvSubplot.setRangeAxis(1, pwatvAxis);
		pwtvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 2nd dataset
		pwtvSubplot.setDataset(1, pwatvDataset);
		// map the 2nd dataset to 2nd axis
		pwtvSubplot.mapDatasetToRangeAxis(1, 1);
		// renderer for average transaction value
		XYLineAndShapeRenderer pwatvRenderer = new XYLineAndShapeRenderer();
		pwatvRenderer.setSeriesPaint(0, Color.BLUE);
		pwatvRenderer.setSeriesLinesVisible(0, false);
		pwatvRenderer.setSeriesShapesVisible(0, true);
		pwatvRenderer.setSeriesShape(0, StockChartUtils.getUpTriangleShape());
		//
		pwtvSubplot.setRenderer(1, pwatvRenderer);
		// 3rd axis for call warrant transaction
		NumberAxis pwtAxis = new NumberAxis("交易筆数-認售");
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
		pwtRenderer.setSeriesPaint(0, Color.ORANGE);
		pwtvSubplot.setRenderer(2, pwtRenderer);
		pwtvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return pwtvSubplot;
	}

	private TimeSeriesCollection createPutWarrantTradeValueDataset() {
		TimeSeriesCollection pwtvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("交易额");
		// add data
		pwtsList.stream().forEach(pwts -> {
			series1.add(new Day(pwts.getTradingDate()), pwts.getTradeValue() / 1000000);
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
			series1.add(new Day(pwts.getTradingDate()), pwts.getAvgTransactionValue() / 1000);
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

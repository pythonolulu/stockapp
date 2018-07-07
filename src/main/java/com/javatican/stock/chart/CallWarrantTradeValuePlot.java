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
import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.StockItem;

@Component("cwtvPlot")
public class CallWarrantTradeValuePlot implements JPlot {

	@Autowired
	CallWarrantTradeSummaryDAO callWarrantTradeSummaryDAO;
	private List<CallWarrantTradeSummary> cwtsList;

	public CallWarrantTradeValuePlot() {
	}
	
	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.cwtsList = callWarrantTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
		if(cwtsList.isEmpty()) return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection cwtvDataset = createCallWarrantTradeValueDataset();
		TimeSeriesCollection cwatvDataset = createCallWarrantAvgTransValueDataset();
		TimeSeriesCollection cwtDataset = createCallWarrantTransactionDataset();
		// Create call warrant trade value chart cwtvAxis
		NumberAxis cwtvAxis = new NumberAxis("交易额-認購(百万)");
		cwtvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		cwtvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create call warrant trade value chart renderer
		XYBarRenderer cwtvRenderer = new XYBarRenderer();
		cwtvRenderer.setShadowVisible(false);
		cwtvRenderer.setSeriesPaint(0, Color.GRAY);
		// Create call warrant trade value Subplot
		XYPlot cwtvSubplot = new XYPlot(cwtvDataset, null, cwtvAxis, cwtvRenderer);
		cwtvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd axis for average transaction value
		NumberAxis cwatvAxis = new NumberAxis("每筆均交易额-認購(千)");
		cwatvAxis.setAutoRangeIncludesZero(true);
		cwtvSubplot.setRangeAxis(1, cwatvAxis);
		cwtvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 2nd dataset
		cwtvSubplot.setDataset(1, cwatvDataset);
		// map the 2nd dataset to 2nd axis
		cwtvSubplot.mapDatasetToRangeAxis(1, 1);
		// renderer for average transaction value
		XYLineAndShapeRenderer cwatvRenderer = new XYLineAndShapeRenderer();
		Shape tri = ShapeUtils.createUpTriangle(1.5F);
		cwatvRenderer.setSeriesPaint(0, Color.BLUE);
		cwatvRenderer.setSeriesLinesVisible(0, false);
		cwatvRenderer.setSeriesShapesVisible(0, true);
		cwatvRenderer.setSeriesShape(0, tri);
		//
		cwtvSubplot.setRenderer(1, cwatvRenderer);
		// 3rd axis for call warrant transaction
		NumberAxis cwtAxis = new NumberAxis("交易筆数-認購");
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
		cwtRenderer.setSeriesPaint(0, Color.ORANGE);
		cwtvSubplot.setRenderer(2, cwtRenderer);
		cwtvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return cwtvSubplot;
	}

	private TimeSeriesCollection createCallWarrantTradeValueDataset() {
		TimeSeriesCollection cwtvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("交易额");
		// add data
		cwtsList.stream().forEach(cwts -> {
			series1.add(new Day(cwts.getTradingDate()), cwts.getTradeValue() / 1000000);
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
			series1.add(new Day(cwts.getTradingDate()), cwts.getAvgTransactionValue() / 1000);
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

}

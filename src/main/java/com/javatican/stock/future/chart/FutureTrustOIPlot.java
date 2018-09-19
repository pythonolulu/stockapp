package com.javatican.stock.future.chart;

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
import com.javatican.stock.dao.FutureDataDAO;
import com.javatican.stock.model.FutureData;
import com.javatican.stock.util.StockChartUtils;

@Component("ftoiPlot")
public class FutureTrustOIPlot {

	@Autowired
	FutureDataDAO futureDataDAO;

	private List<FutureData> fdList;

	public Plot getPlot() throws StockException {
		fdList = futureDataDAO.findAll();
		return createPlot();
	}

	public FutureTrustOIPlot() {
	}

	private XYPlot createPlot() {
		TimeSeriesCollection toDataset = createTrustOiDataset();
		TimeSeriesCollection tnvDataset = createTrustNetVolumeDataset();
		TimeSeriesCollection tvDataset = createTrustVolumeDataset();
		// trust OI axis
		NumberAxis toAxis = new NumberAxis("投信未平仓(口)");
		toAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		toAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// trust trade volume fvAxis
		NumberAxis tvAxis = new NumberAxis("投信交易量(口)");
		tvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		tvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer toRenderer = new XYLineAndShapeRenderer();
		toRenderer.setDefaultShapesVisible(true);
		toRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		toRenderer.setSeriesPaint(0, Color.MAGENTA);
		toRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShape());
		toRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		toRenderer.setSeriesPaint(1, Color.GREEN);
		toRenderer.setSeriesShape(1, StockChartUtils.getSolidSphereShape());
		toRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
		toRenderer.setSeriesPaint(2, Color.BLACK);
		toRenderer.setSeriesShape(2, StockChartUtils.getSolidSphereShape());
		toRenderer.setDefaultSeriesVisibleInLegend(false);
		// net volume renderer
		XYLineAndShapeRenderer tnvRenderer = new XYLineAndShapeRenderer();
		tnvRenderer.setSeriesPaint(0, Color.RED);
		tnvRenderer.setSeriesLinesVisible(0, false);
		tnvRenderer.setSeriesShapesVisible(0, true);
		tnvRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		//
		tnvRenderer.setSeriesPaint(1, Color.BLUE);
		tnvRenderer.setSeriesLinesVisible(1, false);
		tnvRenderer.setSeriesShapesVisible(1, true);
		tnvRenderer.setSeriesShape(1, StockChartUtils.getDownTriangleShape());
		tnvRenderer.setDefaultSeriesVisibleInLegend(false);
		// volume renderer
		XYBarRenderer tvRenderer = new XYBarRenderer();
		tvRenderer.setShadowVisible(false);
		tvRenderer.setSeriesPaint(0, Color.ORANGE);
		tvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		tvRenderer.setDefaultSeriesVisibleInLegend(false);
		// Create trust Subplot
		XYPlot toSubplot = new XYPlot(toDataset, null, toAxis, toRenderer);
		// 2nd dataset
		toSubplot.setDataset(1, tnvDataset);
		// 3rd dataset
		toSubplot.setDataset(2, tvDataset);
		// renderers: dataset 1 uses fnvRenderer, dataset 2 uses fvRenderer
		toSubplot.setRenderer(1, tnvRenderer);
		toSubplot.setRenderer(2, tvRenderer);
		// 2nd axis
		toSubplot.setRangeAxis(1, tvAxis);
		// map the 2nd dataset to 2nd axis
		toSubplot.mapDatasetToRangeAxis(1, 1);
		// map the 3rd dataset to 2nd axis
		toSubplot.mapDatasetToRangeAxis(2, 1);
		//
		toSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		toSubplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
		toSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		toSubplot.setBackgroundPaint(Color.WHITE);
		return toSubplot;
	}

	private TimeSeriesCollection createTrustVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("买多");
		TimeSeries series2 = new TimeSeries("卖空");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getTrustTradingLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getTrustTradingShort());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

	private TimeSeriesCollection createTrustNetVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("净买多");
		TimeSeries series2 = new TimeSeries("净卖空");
		// add data
		fdList.stream().forEach(fd -> {
			double amount = fd.getTrustTradingNet();
			if (amount > 0) {
				series1.add(new Day(fd.getTradingDate()), amount);
			} else {
				series2.add(new Day(fd.getTradingDate()), amount);
			}
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

	private TimeSeriesCollection createTrustOiDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("未平仓(多)");
		TimeSeries series2 = new TimeSeries("未平仓(空)");
		TimeSeries series3 = new TimeSeries("净未平仓");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getTrustOpenLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getTrustOpenShort());
			series3.add(new Day(fd.getTradingDate()), fd.getTrustOpenNet());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		return dataset;
	}

}

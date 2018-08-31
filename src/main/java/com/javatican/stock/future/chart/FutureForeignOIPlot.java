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

@Component("ffoiPlot")
public class FutureForeignOIPlot {

	@Autowired
	FutureDataDAO futureDataDAO;

	private List<FutureData> fdList;

	public Plot getPlot() throws StockException {
		fdList = futureDataDAO.findAll();
		return createPlot();
	}

	public FutureForeignOIPlot() {
	}

	private XYPlot createPlot() {
		TimeSeriesCollection foDataset = createForeignOiDataset();
		TimeSeriesCollection fnvDataset = createForeignNetVolumeDataset();
		TimeSeriesCollection fvDataset = createForeignVolumeDataset();
		//foreign OI axis
		NumberAxis foAxis = new NumberAxis("外资未平仓(口)");
		foAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		foAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//foreign trade volume fvAxis
		NumberAxis fvAxis = new NumberAxis("外资交易量(口)");
		fvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		fvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer foRenderer = new XYLineAndShapeRenderer();
		foRenderer.setDefaultShapesVisible(true);
		foRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(0, Color.MAGENTA);
		foRenderer.setSeriesShape(0, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(1, Color.GREEN);
		foRenderer.setSeriesShape(1, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(2, Color.BLACK);
		foRenderer.setSeriesShape(2, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		//net volume renderer
		XYLineAndShapeRenderer fnvRenderer = new XYLineAndShapeRenderer();
		fnvRenderer.setSeriesPaint(0, Color.RED);
		fnvRenderer.setSeriesLinesVisible(0, false);
		fnvRenderer.setSeriesShapesVisible(0, true);
		fnvRenderer.setSeriesShape(0, new Ellipse2D.Double(-2d, -2d, 4d, 4d));
		//
		fnvRenderer.setSeriesPaint(1, Color.BLUE);
		fnvRenderer.setSeriesLinesVisible(1, false);
		fnvRenderer.setSeriesShapesVisible(1, true);
		Shape tri = ShapeUtils.createDownTriangle(1.5F);
		fnvRenderer.setSeriesShape(1, tri);
		//volume renderer
		XYBarRenderer fvRenderer = new XYBarRenderer();
		fvRenderer.setShadowVisible(false);
		fvRenderer.setSeriesPaint(0, Color.ORANGE);
		fvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		// Create foreign Subplot
		XYPlot foSubplot = new XYPlot(foDataset, null, foAxis, foRenderer);
		// 2nd dataset
		foSubplot.setDataset(1, fnvDataset);
		// 3rd dataset
		foSubplot.setDataset(2, fvDataset);
		//renderers: dataset 1 uses fnvRenderer, dataset 2 uses fvRenderer
		foSubplot.setRenderer(1, fnvRenderer);
		foSubplot.setRenderer(2, fvRenderer);
		//2nd axis
		foSubplot.setRangeAxis(1, fvAxis);
		// map the 2nd dataset to 2nd axis
		foSubplot.mapDatasetToRangeAxis(1, 1);
		// map the 3rd dataset to 2nd axis
		foSubplot.mapDatasetToRangeAxis(2, 1);
		//
		foSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		foSubplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
		foSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		foSubplot.setBackgroundPaint(Color.WHITE);
		return foSubplot;
	}

	private TimeSeriesCollection createForeignVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("买多");
		TimeSeries series2 = new TimeSeries("卖空");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getForeignTradingLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getForeignTradingShort());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

	private TimeSeriesCollection createForeignNetVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("净买多");
		TimeSeries series2 = new TimeSeries("净卖空");
		// add data
		fdList.stream().forEach(fd -> {
			double amount = fd.getForeignTradingNet();
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

	private TimeSeriesCollection createForeignOiDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("未平仓(多)");
		TimeSeries series2 = new TimeSeries("未平仓(空)");
		TimeSeries series3 = new TimeSeries("净未平仓");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getForeignOpenLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getForeignOpenShort());
			series3.add(new Day(fd.getTradingDate()), fd.getForeignOpenNet());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		return dataset;
	}

}

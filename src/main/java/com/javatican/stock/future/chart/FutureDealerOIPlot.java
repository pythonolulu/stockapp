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

@Component("fdoiPlot")
public class FutureDealerOIPlot {

	@Autowired
	FutureDataDAO futureDataDAO;

	private List<FutureData> fdList;

	public Plot getPlot() throws StockException {
		fdList = futureDataDAO.findAll();
		return createPlot();
	}

	public FutureDealerOIPlot() {
	}

	private XYPlot createPlot() {
		TimeSeriesCollection doDataset = createDealerOiDataset();
		TimeSeriesCollection dnvDataset = createDealerNetVolumeDataset();
		TimeSeriesCollection dvDataset = createDealerVolumeDataset();
		//dealer OI axis
		NumberAxis doAxis = new NumberAxis("自营商未平仓(口)");
		doAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		doAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//dealer trade volume fvAxis
		NumberAxis dvAxis = new NumberAxis("自营商交易量(口)");
		dvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		dvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer doRenderer = new XYLineAndShapeRenderer();
		doRenderer.setDefaultShapesVisible(true);
		doRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		doRenderer.setSeriesPaint(0, Color.MAGENTA);
		doRenderer.setSeriesShape(0, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		doRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		doRenderer.setSeriesPaint(1, Color.GREEN);
		doRenderer.setSeriesShape(1, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		doRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
		doRenderer.setSeriesPaint(2, Color.BLACK);
		doRenderer.setSeriesShape(2, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		doRenderer.setDefaultSeriesVisibleInLegend(false); 
		//net volume renderer
		XYLineAndShapeRenderer dnvRenderer = new XYLineAndShapeRenderer();
		dnvRenderer.setSeriesPaint(0, Color.RED);
		dnvRenderer.setSeriesLinesVisible(0, false);
		dnvRenderer.setSeriesShapesVisible(0, true);
		dnvRenderer.setSeriesShape(0, new Ellipse2D.Double(-2d, -2d, 4d, 4d));
		//
		dnvRenderer.setSeriesPaint(1, Color.BLUE);
		dnvRenderer.setSeriesLinesVisible(1, false);
		dnvRenderer.setSeriesShapesVisible(1, true);
		Shape tri = ShapeUtils.createDownTriangle(1.5F);
		dnvRenderer.setSeriesShape(1, tri);
		dnvRenderer.setDefaultSeriesVisibleInLegend(false); 
		//volume renderer
		XYBarRenderer dvRenderer = new XYBarRenderer();
		dvRenderer.setShadowVisible(false);
		dvRenderer.setSeriesPaint(0, Color.ORANGE);
		dvRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		dvRenderer.setDefaultSeriesVisibleInLegend(false); 
		// Create dealer Subplot
		XYPlot doSubplot = new XYPlot(doDataset, null, doAxis, doRenderer);
		// 2nd dataset
		doSubplot.setDataset(1, dnvDataset);
		// 3rd dataset
		doSubplot.setDataset(2, dvDataset);
		//renderers: dataset 1 uses fnvRenderer, dataset 2 uses fvRenderer
		doSubplot.setRenderer(1, dnvRenderer);
		doSubplot.setRenderer(2, dvRenderer);
		//2nd axis
		doSubplot.setRangeAxis(1, dvAxis);
		// map the 2nd dataset to 2nd axis
		doSubplot.mapDatasetToRangeAxis(1, 1);
		// map the 3rd dataset to 2nd axis
		doSubplot.mapDatasetToRangeAxis(2, 1);
		//
		doSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		doSubplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
		doSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		doSubplot.setBackgroundPaint(Color.WHITE);
		return doSubplot;
	}

	private TimeSeriesCollection createDealerVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("买多");
		TimeSeries series2 = new TimeSeries("卖空");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getDealerTradingLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getDealerTradingShort());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

	private TimeSeriesCollection createDealerNetVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("净买多");
		TimeSeries series2 = new TimeSeries("净卖空");
		// add data
		fdList.stream().forEach(fd -> {
			double amount = fd.getDealerTradingNet();
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

	private TimeSeriesCollection createDealerOiDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("未平仓(多)");
		TimeSeries series2 = new TimeSeries("未平仓(空)");
		TimeSeries series3 = new TimeSeries("净未平仓");
		// add data
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getDealerOpenLong());
			series2.add(new Day(fd.getTradingDate()), -1 * fd.getDealerOpenShort());
			series3.add(new Day(fd.getTradingDate()), fd.getDealerOpenNet());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		return dataset;
	}

}

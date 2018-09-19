package com.javatican.stock.option.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;

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
import com.javatican.stock.dao.OptionDataDAO;
import com.javatican.stock.model.OptionData;
import com.javatican.stock.util.StockChartUtils;
import com.javatican.stock.util.StockUtils;

@Component("optionOIPlot")
public class OptionOIPlot {

	@Autowired
	OptionDataDAO optionDataDAO;

	private List<OptionData> odList;

	public Plot getPlot() throws StockException {
		odList = optionDataDAO.findAll();
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection ooiDataset = createOptionOiDataset();
		TimeSeriesCollection ovDataset = createOptionVolumeDataset(); 
		TimeSeriesCollection pcrDataset = createPutCallRatioDataset();
		// option OI axis
		NumberAxis ooiAxis = new NumberAxis("未平仓(口)");
		ooiAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ooiAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// option put/call ratio pcrAxis
		NumberAxis pcrAxis = new NumberAxis("卖權买權比例");
		// option trade volume ovAxis
		NumberAxis ovAxis = new NumberAxis("交易量(口)");
		ovAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ovAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer ooiRenderer = new XYLineAndShapeRenderer();
		ooiRenderer.setDefaultShapesVisible(true);
		ooiRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		ooiRenderer.setSeriesPaint(0, Color.MAGENTA);
		ooiRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShape());
		ooiRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		ooiRenderer.setSeriesPaint(1, Color.BLUE);
		ooiRenderer.setSeriesShape(1, StockChartUtils.getSolidSphereShape());
		// Put/Call ratio renderer
		XYLineAndShapeRenderer pcrRenderer = new XYLineAndShapeRenderer();
		pcrRenderer.setDefaultShapesVisible(true);
		pcrRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		pcrRenderer.setSeriesPaint(0, Color.BLACK);
		pcrRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		pcrRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		pcrRenderer.setSeriesPaint(1, Color.RED);
		pcrRenderer.setSeriesShape(1, StockChartUtils.getUpTriangleShape());
		// volume renderer
		XYBarRenderer ovRenderer = new XYBarRenderer();
		ovRenderer.setShadowVisible(false);
		ovRenderer.setSeriesPaint(0, Color.ORANGE);
		ovRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		
		// Create option Subplot
		XYPlot ooiSubplot = new XYPlot(ooiDataset, null, ooiAxis, ooiRenderer);
		// 2nd dataset
		ooiSubplot.setDataset(1, pcrDataset);
		// 3rd dataset
		ooiSubplot.setDataset(2, ovDataset);
		// renderers: dataset 2 uses pcrRenderer, dataset 3 uses ovRenderer
		ooiSubplot.setRenderer(1, pcrRenderer);
		ooiSubplot.setRenderer(2, ovRenderer);
		// 2nd axis
		ooiSubplot.setRangeAxis(1, pcrAxis);
		// 3rd axis
		ooiSubplot.setRangeAxis(2, ovAxis);
		// map the 2nd dataset to 2nd axis
		ooiSubplot.mapDatasetToRangeAxis(1, 1);
		// map the 3rd dataset to 3rd axis
		ooiSubplot.mapDatasetToRangeAxis(2, 2);
		//
		ooiSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		ooiSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		ooiSubplot.setRangeAxisLocation(2, AxisLocation.TOP_OR_LEFT);
		ooiSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		ooiSubplot.setBackgroundPaint(Color.WHITE);
		StockChartUtils.drawHorizontalValueMarker(ooiSubplot, 1, 1.0);
		return ooiSubplot;
	}

	private TimeSeriesCollection createOptionVolumeDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("交易量(买權)");
		TimeSeries series2 = new TimeSeries("交易量(卖權)");
		// add data
		odList.stream().forEach(od -> {
			series1.add(new Day(od.getTradingDate()), od.getCallTradingVolume());
			series2.add(new Day(od.getTradingDate()), -1 * od.getPutTradingVolume());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}
 

	private TimeSeriesCollection createOptionOiDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("未平仓(买權)");
		TimeSeries series2 = new TimeSeries("未平仓(卖權)");  
		// add data
		odList.stream().forEach(od -> {
			series1.add(new Day(od.getTradingDate()), od.getCallOi());
			series2.add(new Day(od.getTradingDate()), -1 * od.getPutOi()); 
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

	private TimeSeriesCollection createPutCallRatioDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("卖權买權OI比");
		TimeSeries series2 = new TimeSeries("卖權买權交易量比");
		// add data
		odList.stream().forEach(od -> {
			series1.add(new Day(od.getTradingDate()), StockUtils.roundDoubleDp2(od.getPutOi() / od.getCallOi()));
			series2.add(new Day(od.getTradingDate()), StockUtils.roundDoubleDp2(od.getPutTradingVolume() / od.getCallTradingVolume()));
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		return dataset;
	}

}

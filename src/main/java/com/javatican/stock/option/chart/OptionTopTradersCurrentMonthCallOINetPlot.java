package com.javatican.stock.option.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Component;

import com.javatican.stock.util.StockChartUtils;

@Component("ottcmcoinPlot")
public class OptionTopTradersCurrentMonthCallOINetPlot extends OptionParentPlot{
 
	public OptionTopTradersCurrentMonthCallOINetPlot() {
	}

	protected XYPlot createPlot() {
		TimeSeriesCollection dataset1 = createOIDataset();
		// foreign OI axis
		NumberAxis axis1 = new NumberAxis("未平仓(口)");
		axis1.setAutoRangeIncludesZero(true);
		// Set to no decimal
		axis1.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
		renderer1.setDefaultShapesVisible(true);
		renderer1.setSeriesStroke(0, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(0, Color.MAGENTA);
		renderer1.setSeriesShape(0, StockChartUtils.getUpTriangleShape());
		renderer1.setSeriesStroke(1, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(1, Color.GREEN);
		renderer1.setSeriesShape(1,StockChartUtils.getUpTriangleShape());
		renderer1.setSeriesStroke(2, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(2, Color.BLACK);
		renderer1.setSeriesShape(2, StockChartUtils.getUpTriangleShape());
		// Create foreign Subplot
		XYPlot subplot = new XYPlot(dataset1, null, axis1, renderer1);
		subplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		subplot.setBackgroundPaint(Color.WHITE);
		StockChartUtils.drawHorizontalValueMarker(subplot, 0, 0);
		return subplot;
	}

	private TimeSeriesCollection createOIDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("前十大");
		TimeSeries series2 = new TimeSeries("前五大");
		TimeSeries series3 = new TimeSeries("六到十");
		// add data
		odList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getCallBuyOiTop10() - fd.getCallSellOiTop10());
			series2.add(new Day(fd.getTradingDate()), fd.getCallBuyOiTop5() - fd.getCallSellOiTop5());
			series3.add(new Day(fd.getTradingDate()),
					fd.getCallBuyOiTop10() - fd.getCallBuyOiTop5() - fd.getCallSellOiTop10() + fd.getCallSellOiTop5());
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		return dataset;
	}

}

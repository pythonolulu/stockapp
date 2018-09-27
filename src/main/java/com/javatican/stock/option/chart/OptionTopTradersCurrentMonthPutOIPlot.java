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

@Component("ottcmpoiPlot")
public class OptionTopTradersCurrentMonthPutOIPlot extends OptionParentPlot{
 
	public OptionTopTradersCurrentMonthPutOIPlot() {
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
		renderer1.setSeriesShape(0, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(1, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(1, Color.GREEN);
		renderer1.setSeriesShape(1, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(2, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(2, Color.BLACK);
		renderer1.setSeriesShape(2, StockChartUtils.getSolidSphereShape());
		//
		renderer1.setSeriesStroke(3, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(3, Color.RED);
		renderer1.setSeriesShape(3, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(4, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(4, Color.ORANGE);
		renderer1.setSeriesShape(4, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(5, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(5, Color.BLUE);
		renderer1.setSeriesShape(5, StockChartUtils.getSolidSphereShape());
		renderer1.setDefaultSeriesVisibleInLegend(false);
		// Create foreign Subplot
		XYPlot subplot = new XYPlot(dataset1, null, axis1, renderer1);
		subplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		subplot.setBackgroundPaint(Color.WHITE);
		return subplot;
	}

	private TimeSeriesCollection createOIDataset() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("前十大(多)");
		TimeSeries series2 = new TimeSeries("前五大(多)");
		TimeSeries series3 = new TimeSeries("六到十(多)");
		TimeSeries series4 = new TimeSeries("前十大(空)");
		TimeSeries series5 = new TimeSeries("前五大(空)");
		TimeSeries series6 = new TimeSeries("六到十(空)");
		// add data
		odList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getPutBuyOiTop10());
			series2.add(new Day(fd.getTradingDate()), fd.getPutBuyOiTop5());
			series3.add(new Day(fd.getTradingDate()), fd.getPutBuyOiTop10() - fd.getPutBuyOiTop5());
			series4.add(new Day(fd.getTradingDate()), -1 * fd.getPutSellOiTop10());
			series5.add(new Day(fd.getTradingDate()), -1 * fd.getPutSellOiTop5());
			series6.add(new Day(fd.getTradingDate()), -1 * (fd.getPutSellOiTop10() - fd.getPutSellOiTop5()));
		});
		// add data
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		dataset.addSeries(series4);
		dataset.addSeries(series5);
		dataset.addSeries(series6);
		return dataset;
	}

}

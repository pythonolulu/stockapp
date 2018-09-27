package com.javatican.stock.option.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.springframework.stereotype.Component;

import com.javatican.stock.util.StockChartUtils;

@Component("otoiuPlot")
public class OptionTrustOIUnitPlot extends OptionParentPlot{
 
	public OptionTrustOIUnitPlot() {
	}

	protected XYPlot createPlot() {
		TableXYDataset dataset1 = createNetOIDataset();
		TableXYDataset dataset2 = createLongPositionOIDataset();
		TableXYDataset dataset3 = createShortPositionOIDataset();
		// OI Axis
		NumberAxis axis2 = new NumberAxis("未平仓口数");
		axis2.setAutoRangeIncludesZero(true);
		// Set to no decimal
		axis2.setNumberFormatOverride(new DecimalFormat("###,###"));
		// net OI renderer
		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
		renderer1.setDefaultSeriesVisibleInLegend(false);
		renderer1.setDefaultShapesVisible(true);
		renderer1.setSeriesStroke(0, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(0, Color.BLUE);
		renderer1.setSeriesShape(0, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(1, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(1, Color.RED);
		renderer1.setSeriesShape(1, StockChartUtils.getSolidSphereShape());
		renderer1.setSeriesStroke(2, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(2, Color.BLACK);
		renderer1.setSeriesShape(2, StockChartUtils.getSolidSphereShape());
		// long OI renderer
		StackedXYBarRenderer renderer2 = new StackedXYBarRenderer(0.15);
		renderer2.setDrawBarOutline(false);
		renderer2.setShadowVisible(false);
		renderer2.setDefaultSeriesVisibleInLegend(false);
		renderer2.setSeriesPaint(0, Color.ORANGE);
		renderer2.setSeriesPaint(1, Color.GREEN);
		// long OI renderer
		StackedXYBarRenderer renderer3 = new StackedXYBarRenderer(0.15);
		renderer3.setDrawBarOutline(false);
		renderer3.setShadowVisible(false);
		renderer3.setDefaultSeriesVisibleInLegend(false);
		renderer3.setSeriesPaint(0, Color.CYAN);
		renderer3.setSeriesPaint(1, Color.MAGENTA);
		// Create subplot
		XYPlot subplot = new XYPlot(dataset1, null, axis2, renderer1);
		// 2nd dataset
		subplot.setDataset(1, dataset2);
		// 3rd dataset
		subplot.setDataset(2, dataset3);
		// renderers: dataset2 uses renderer2, dataset3 uses renderer3
		subplot.setRenderer(1, renderer2);
		subplot.setRenderer(2, renderer3);
		subplot.mapDatasetToRangeAxis(1, 0);
		subplot.mapDatasetToRangeAxis(2, 0);
		//
		subplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		subplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		subplot.setBackgroundPaint(Color.WHITE);
		StockChartUtils.drawHorizontalValueMarker(subplot, 0, 0);
		return subplot;
	} 

	/*
	 * long position includes 1) Call option Buy and 2) Put option Sell
	 */
	private TableXYDataset createLongPositionOIDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		// add data
		odList.stream().forEach(od -> {
			dataset.add(new Day(od.getTradingDate()), od.getCallTrustOpenLong(), "买權买方");
			dataset.add(new Day(od.getTradingDate()), od.getPutTrustOpenShort(), "卖權卖方");
		});
		return dataset;
	}

	/*
	 * short position includes 1) Put option Buy and 2) Call option Sell
	 */
	private TableXYDataset createShortPositionOIDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		// add data
		odList.stream().forEach(od -> {
			dataset.add(new Day(od.getTradingDate()), -1 * od.getPutTrustOpenLong(), "卖權买方");
			dataset.add(new Day(od.getTradingDate()), -1 * od.getCallTrustOpenShort(), "买權卖方");
		});
		return dataset;
	}

	private TableXYDataset createNetOIDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		// add data
		odList.stream().forEach(od -> {
			dataset.add(new Day(od.getTradingDate()), od.getCallTrustOpenNet(), "买權差额(买-卖)");
			dataset.add(new Day(od.getTradingDate()), -1 * od.getPutTrustOpenNet(), "卖權差额(卖-买)");
			dataset.add(new Day(od.getTradingDate()), od.getCallTrustOpenNet() - od.getPutTrustOpenNet(), "买卖權净差额");
		});
		return dataset;
	}

}

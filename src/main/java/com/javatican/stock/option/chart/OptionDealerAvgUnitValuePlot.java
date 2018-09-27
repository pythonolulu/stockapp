package com.javatican.stock.option.chart;

import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.springframework.stereotype.Component;

import com.javatican.stock.util.StockChartUtils;

@Component("odauvPlot")
public class OptionDealerAvgUnitValuePlot extends OptionParentPlot{

	public OptionDealerAvgUnitValuePlot() {
	}

	protected XYPlot createPlot() {
		TableXYDataset dataset2 = createLongPositionOIDataset();
		TableXYDataset dataset3 = createShortPositionOIDataset();
		// OI Axis
		NumberAxis axis2 = new NumberAxis("平均契約價");
		axis2.setAutoRangeIncludesZero(true);
		// Set to no decimal
		axis2.setNumberFormatOverride(new DecimalFormat("###,###"));
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
		XYPlot subplot = new XYPlot(dataset2, null, axis2, renderer2);
		// 2nd dataset
		subplot.setDataset(1, dataset3); 
		// renderers: dataset2 uses renderer2, dataset3 uses renderer3
		subplot.setRenderer(1, renderer3);
		subplot.mapDatasetToRangeAxis(1, 0); 
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
			dataset.add(new Day(od.getTradingDate()), od.getCallDealerOpenValueLong()*1000 / od.getCallDealerOpenLong(),
					"买權买方");
			dataset.add(new Day(od.getTradingDate()), od.getPutDealerOpenValueShort()*1000 / od.getPutDealerOpenLong(),
					"卖權卖方");
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
			dataset.add(new Day(od.getTradingDate()), -1 * od.getPutDealerOpenValueLong()*1000 / od.getPutDealerOpenLong(),
					"卖權买方");
			dataset.add(new Day(od.getTradingDate()),
					-1 * od.getCallDealerOpenValueShort()*1000 / od.getCallDealerOpenShort(), "买權卖方");
		});
		return dataset;
	}
}

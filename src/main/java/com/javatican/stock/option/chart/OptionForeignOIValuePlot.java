package com.javatican.stock.option.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.OptionDataDAO;
import com.javatican.stock.model.OptionData;

@Component("ofoivPlot")
public class OptionForeignOIValuePlot {

	@Autowired
	OptionDataDAO optionDataDAO;

	private List<OptionData> odList;

	public Plot getPlot() throws StockException {
		odList = optionDataDAO.findAll();
		return createPlot();
	}

	public OptionForeignOIValuePlot() {
	}

	private XYPlot createPlot() {
		TableXYDataset dataset1 = createNetOIDataset();
		TableXYDataset dataset2 = createLongPositionOIDataset();
		TableXYDataset dataset3 = createShortPositionOIDataset();
		// net OI axis
		NumberAxis axis1 = new NumberAxis("差额(千元)");
		axis1.setAutoRangeIncludesZero(true);
		// Set to no decimal
		axis1.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI Axis
		NumberAxis axis2 = new NumberAxis("契约金额(千元)");
		axis2.setAutoRangeIncludesZero(true);
		// Set to no decimal
		axis2.setNumberFormatOverride(new DecimalFormat("###,###"));
		// net OI renderer
		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
		renderer1.setDefaultShapesVisible(true);
		renderer1.setDefaultSeriesVisibleInLegend(true);
		renderer1.setSeriesStroke(0, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(0, Color.MAGENTA);
		renderer1.setSeriesShape(0, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(1, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(1, Color.GREEN);
		renderer1.setSeriesShape(1, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(2, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(2, Color.BLACK);
		renderer1.setSeriesShape(2, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		// long OI renderer
		StackedXYBarRenderer renderer2 = new StackedXYBarRenderer(0.15);
		renderer2.setDrawBarOutline(false);
		renderer2.setShadowVisible(false);
		renderer2.setDefaultSeriesVisibleInLegend(true);
		renderer2.setSeriesPaint(0, Color.RED);
		renderer2.setSeriesPaint(1, Color.ORANGE);
		// long OI renderer
		StackedXYBarRenderer renderer3 = new StackedXYBarRenderer(0.15);
		renderer3.setDrawBarOutline(false);
		renderer3.setShadowVisible(false);
		renderer3.setDefaultSeriesVisibleInLegend(true);
		renderer3.setSeriesPaint(0, Color.BLUE);
		renderer3.setSeriesPaint(1, Color.CYAN);
		// Create subplot
		XYPlot subplot = new XYPlot(dataset1, null, axis1, renderer1);
		// 2nd dataset
		subplot.setDataset(1, dataset2);
		// 3rd dataset
		subplot.setDataset(2, dataset3);
		// renderers: dataset2 uses renderer2, dataset3 uses renderer3
		subplot.setRenderer(1, renderer2);
		subplot.setRenderer(2, renderer3);
		// 2nd axis
		subplot.setRangeAxis(1, axis2);
		// map the 2nd dataset to 2nd axis
		subplot.mapDatasetToRangeAxis(1, 1);
		// map the 3rd dataset to 2nd axis
		subplot.mapDatasetToRangeAxis(2, 1);
		//
		subplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		subplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
		subplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		subplot.setBackgroundPaint(Color.WHITE);
		return subplot;
	}

	/*
	 * long position includes 1) Call option Buy and 2) Put option Sell
	 */
	private TableXYDataset createLongPositionOIDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		// add data
		odList.stream().forEach(od -> {
			dataset.add(new Day(od.getTradingDate()), od.getCallForeignOpenValueLong(), "买權买方");
			dataset.add(new Day(od.getTradingDate()), od.getPutForeignOpenValueShort(), "卖權卖方");
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
			dataset.add(new Day(od.getTradingDate()), -1 * od.getPutForeignOpenValueLong(), "卖權买方");
			dataset.add(new Day(od.getTradingDate()), -1 * od.getCallForeignOpenValueShort(), "买權卖方");
		});
		return dataset;
	}

	private TableXYDataset createNetOIDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		// add data
		odList.stream().forEach(od -> {
			dataset.add(new Day(od.getTradingDate()), od.getCallForeignOpenValueNet(), "买權差额(买-卖)");
			dataset.add(new Day(od.getTradingDate()), -1 * od.getPutForeignOpenValueNet(), "卖權差额(卖-买)");
			dataset.add(new Day(od.getTradingDate()), od.getCallForeignOpenValueNet() - od.getPutForeignOpenValueNet(),
					"买卖權净差额");
		});
		return dataset;
	}

}

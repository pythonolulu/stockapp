package com.javatican.stock.option.series.chart;

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

@Component("optionSeriesOIPlot")
public class OptionSeriesOIPlot extends OptionSeriesParentPlot {

	protected XYPlot createPlot() {
		TableXYDataset oiDataset = createOpenInterestDataset();
		TableXYDataset volumeDataset = createVolumeDataset();
		// OI axis
		NumberAxis oiAxis = new NumberAxis("未平仓量(口)");
		oiAxis.setAutoRangeIncludesZero(true);
		oiAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// volume axis
		NumberAxis volumeAxis = new NumberAxis("交易量(口)");
		volumeAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		volumeAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create OI renderer
		XYLineAndShapeRenderer oiRenderer = new XYLineAndShapeRenderer();
		oiRenderer.setDefaultShapesVisible(false);
		oiRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		oiRenderer.setSeriesPaint(0, Color.BLUE);
		oiRenderer.setSeriesLinesVisible(0, true);
		oiRenderer.setSeriesShapesVisible(0, true);
		oiRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		oiRenderer.setDefaultSeriesVisibleInLegend(true);
		// Create volume chart renderer
		StackedXYBarRenderer volumeRenderer = new StackedXYBarRenderer(0.15);
		volumeRenderer.setDrawBarOutline(false);
		volumeRenderer.setShadowVisible(false);
		volumeRenderer.setDefaultSeriesVisibleInLegend(true);
		volumeRenderer.setSeriesPaint(0, Color.RED);
		volumeRenderer.setSeriesPaint(1, Color.GREEN);

		XYPlot volumeSubplot = new XYPlot(oiDataset, null, oiAxis, oiRenderer);
		volumeSubplot.setBackgroundPaint(Color.WHITE);
		volumeSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		volumeSubplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);

		volumeSubplot.setRangeAxis(1, volumeAxis);
		volumeSubplot.setDataset(1, volumeDataset);
		volumeSubplot.mapDatasetToRangeAxis(1, 1);

		volumeSubplot.setRenderer(1, volumeRenderer);
		volumeSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		return volumeSubplot;
	}

	private TableXYDataset createVolumeDataset() {
		TimeTableXYDataset volumeDataset = new TimeTableXYDataset();
		osList.stream().forEach(fd -> {
			volumeDataset.add(new Day(fd.getTradingDate()), fd.getVolumeAfterHour(), "After hour");
			volumeDataset.add(new Day(fd.getTradingDate()), fd.getVolumeRegular(), "Regular");
		});
		return volumeDataset;
	}

	private TableXYDataset createOpenInterestDataset() {
		TimeTableXYDataset oiDataset = new TimeTableXYDataset();
		// add data
		osList.stream().forEach(fd -> {
			oiDataset.add(new Day(fd.getTradingDate()), fd.getOpenInterest(), "OI");
		});
		return oiDataset;
	}

}

package com.javatican.stock.index.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.Date;
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
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.model.TradingValue;
import com.javatican.stock.util.StockChartUtils;

@Component("indexVolumePlot")
public class IndexVolumePlot {

	@Autowired
	TradingValueDAO tradingValueDAO;

	private List<TradingValue> tvList;

	public Plot getPlot() throws StockException {
		tvList = tradingValueDAO.findAll();
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection volumeDataset = createVolumeDataset();
		TimeSeriesCollection kdDataset = createKDDataset();
		//

		// Create volume chart volumeAxis
		NumberAxis volumeAxis = new NumberAxis("交易金額(10億)");
		volumeAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		volumeAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// Create volume chart renderer
		XYBarRenderer volRenderer = new XYBarRenderer();
		volRenderer.setShadowVisible(false);
		volRenderer.setSeriesVisibleInLegend(0, false);
		volRenderer.setSeriesVisibleInLegend(1, false);
		volRenderer.setSeriesVisibleInLegend(2, false);
		volRenderer.setSeriesPaint(0, Color.RED);
		volRenderer.setSeriesPaint(1, Color.GREEN);
		volRenderer.setSeriesPaint(2, Color.GRAY);
		// volRenderer.setDefaultToolTipGenerator(new
		// StandardXYToolTipGenerator("Date={1}, Vol={2}",new
		// SimpleDateFormat("yy/MM/dd"), new DecimalFormat("###,###")));
		// Create volumeSubplot
		XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, volRenderer);
		volumeSubplot.setBackgroundPaint(Color.WHITE);
		volumeSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		//
		// Create k/d chart kdAxis
		NumberAxis kdAxis = new NumberAxis("K9/D9");
		kdAxis.setAutoRangeIncludesZero(true);
		kdAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		volumeSubplot.setRangeAxis(1, kdAxis);
		volumeSubplot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
		volumeSubplot.setDataset(1, kdDataset);
		volumeSubplot.mapDatasetToRangeAxis(1, 1);
		// Create k/d chart renderer
		XYLineAndShapeRenderer kdRenderer = new XYLineAndShapeRenderer();
		kdRenderer.setDefaultShapesVisible(false);
		kdRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		kdRenderer.setSeriesPaint(0, Color.BLUE);
		kdRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		kdRenderer.setSeriesPaint(1, Color.PINK);
		kdRenderer.setSeriesPaint(2, Color.BLACK);
		kdRenderer.setSeriesLinesVisible(2, false);
		kdRenderer.setSeriesShapesVisible(2, true);
		kdRenderer.setSeriesShape(2, StockChartUtils.getUpTriangleShape());
		kdRenderer.setSeriesVisibleInLegend(2, false);
		kdRenderer.setSeriesPaint(3, Color.BLACK);
		kdRenderer.setSeriesLinesVisible(3, false);
		kdRenderer.setSeriesShapesVisible(3, true);
		kdRenderer.setSeriesShape(3, StockChartUtils.getSolidSphereShapeLarge());
		kdRenderer.setSeriesVisibleInLegend(3, false);
		volumeSubplot.setRenderer(1, kdRenderer);
		volumeSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return volumeSubplot;
	}

	private TimeSeriesCollection createVolumeDataset() {
		TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
		TimeSeries pVolumeSeries = new TimeSeries("PVolume");
		TimeSeries nVolumeSeries = new TimeSeries("NVolume");
		TimeSeries zVolumeSeries = new TimeSeries("ZVolume");
		// add data
		Double oldClosePrice = 0.0;
		for (TradingValue tv : tvList) {
			Double closePrice = tv.getClose();
			if (closePrice > oldClosePrice) {
				pVolumeSeries.add(new Day(tv.getTradingDate()), tv.getTotalValue() / 1000000000);
			} else if (closePrice < oldClosePrice) {
				nVolumeSeries.add(new Day(tv.getTradingDate()), tv.getTotalValue() / 1000000000);
			} else {
				zVolumeSeries.add(new Day(tv.getTradingDate()), tv.getTotalValue() / 1000000000);
			}
			oldClosePrice = closePrice;
		}
		volumeDataset.addSeries(pVolumeSeries);
		volumeDataset.addSeries(nVolumeSeries);
		volumeDataset.addSeries(zVolumeSeries);
		return volumeDataset;
	}

	private TimeSeriesCollection createKDDataset() {
		TimeSeriesCollection kdDataset = new TimeSeriesCollection();
		TimeSeries kSeries = new TimeSeries("K9");
		TimeSeries dSeries = new TimeSeries("D9");
		TimeSeries ka80Series = new TimeSeries("ka80");
		TimeSeries ku20Series = new TimeSeries("ku20");
		// add data
		tvList.stream().forEach(tv -> {
			Date date = tv.getTradingDate();
			Day d = new Day(date);
			kSeries.add(d, tv.getK());
			dSeries.add(d, tv.getD());
			if (tv.getK() >= 80) {
				ka80Series.add(d, tv.getK());
			}
			if (tv.getK() <= 20) {
				ku20Series.add(d, tv.getK());
			}
		});
		// add data
		kdDataset.addSeries(kSeries);
		kdDataset.addSeries(dSeries);
		kdDataset.addSeries(ka80Series);
		kdDataset.addSeries(ku20Series);
		return kdDataset;
	}

}

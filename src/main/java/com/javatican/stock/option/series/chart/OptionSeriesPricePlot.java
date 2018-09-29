package com.javatican.stock.option.series.chart;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.springframework.stereotype.Component;

@Component("optionSeriesPricePlot")
public class OptionSeriesPricePlot extends OptionSeriesParentPlot {
	protected XYPlot createPlot() {
		OHLCSeriesCollection priceDataset = createPriceDataset();
		// Create candlestick chart priceAxis
		NumberAxis priceAxis = new NumberAxis("价格");
		priceAxis.setAutoRangeIncludesZero(false);
		// Create candlestick chart renderer
		CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
				false, new HighLowItemLabelGenerator(new SimpleDateFormat("yy/MM/dd"), new DecimalFormat("0.00")));
		candlestickRenderer.setUpPaint(Color.red);
		candlestickRenderer.setDownPaint(Color.green);
		candlestickRenderer.setUseOutlinePaint(true);
		candlestickRenderer.setSeriesVisibleInLegend(0, false);
		// Create candlestickSubplot
		XYPlot candlestickSubplot = new XYPlot(priceDataset, null, priceAxis, candlestickRenderer);

		candlestickSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		candlestickSubplot.setBackgroundPaint(Color.WHITE);
		return candlestickSubplot;
	}

	private OHLCSeriesCollection createPriceDataset() {
		OHLCSeriesCollection priceDataset = new OHLCSeriesCollection();
		OHLCSeries ohlcSeries = new OHLCSeries("Price");
		// add data
		osList.stream().forEach(fd -> ohlcSeries.add(new Day(fd.getTradingDate()), fd.getOpen(), fd.getHigh(),
				fd.getLow(), fd.getClose()));
		priceDataset.addSeries(ohlcSeries);
		return priceDataset;
	}

}

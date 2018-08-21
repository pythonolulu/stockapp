package com.javatican.stock.index.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.TradingValueDAO;
import com.javatican.stock.model.TradingValue;

@Component("indexPricePlot")
public class IndexPricePlot {

	@Autowired
	TradingValueDAO tradingValueDAO;
 
	private List<TradingValue> tvList;

	public Plot getPlot() throws StockException {
		tvList = tradingValueDAO.findAll();
		return createPlot();
	}

	private XYPlot createPlot() {
		OHLCSeriesCollection priceDataset = createPriceDataset();
		TimeSeriesCollection smaDataset = createSMADataset();
		// Create candlestick chart priceAxis
		NumberAxis priceAxis = new NumberAxis("指數");
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
		// create SMA renderer
		XYLineAndShapeRenderer smaRenderer = new XYLineAndShapeRenderer();
		// add 2nd dataset (SMA)
		candlestickSubplot.setDataset(1, smaDataset);
		smaRenderer.setDefaultShapesVisible(false);
		smaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		smaRenderer.setSeriesPaint(0, Color.CYAN);
		smaRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		smaRenderer.setSeriesPaint(1, Color.ORANGE);
		smaRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
		smaRenderer.setSeriesPaint(2, Color.GRAY);
		smaRenderer.setSeriesStroke(3, new BasicStroke(1.0f));
		smaRenderer.setSeriesPaint(3, Color.MAGENTA);
		candlestickSubplot.setRenderer(1, smaRenderer);
		candlestickSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return candlestickSubplot;
	}

	private OHLCSeriesCollection createPriceDataset() {
		OHLCSeriesCollection priceDataset = new OHLCSeriesCollection();
		OHLCSeries ohlcSeries = new OHLCSeries("Price");
		// add data
		tvList.stream().forEach(tv -> ohlcSeries.add(new Day(tv.getTradingDate()), tv.getOpen(),
				tv.getHigh(), tv.getLow(), tv.getClose()));
		priceDataset.addSeries(ohlcSeries);
		return priceDataset;
	}

	private TimeSeriesCollection createSMADataset() {
		TimeSeriesCollection smaDataset = new TimeSeriesCollection();
		TimeSeries sma60Series = new TimeSeries("SMA_60");
		TimeSeries sma20Series = new TimeSeries("SMA_20");
		TimeSeries sma10Series = new TimeSeries("SMA_10");
		TimeSeries sma5Series = new TimeSeries("SMA_5");
		// add data

		tvList.stream().forEach(tv -> {
			Date date = tv.getTradingDate();
			sma60Series.add(new Day(date), tv.getSma60());
			sma20Series.add(new Day(date), tv.getSma20());
			sma10Series.add(new Day(date), tv.getSma10());
			sma5Series.add(new Day(date), tv.getSma5());
		});

		// add data
		smaDataset.addSeries(sma5Series);
		smaDataset.addSeries(sma10Series);
		smaDataset.addSeries(sma20Series);
		smaDataset.addSeries(sma60Series);
		return smaDataset;
	}

}

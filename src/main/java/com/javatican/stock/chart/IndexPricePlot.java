package com.javatican.stock.chart;

import java.util.Date;

import org.jfree.chart.plot.Plot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;

@Component("indexPricePlot")
public class IndexPricePlot extends PricePlot {

	public Plot getPlot() throws StockException {
		return createPlot();
	}

	@Override
	protected OHLCSeriesCollection createPriceDataset() {
		OHLCSeriesCollection priceDataset = new OHLCSeriesCollection();
		OHLCSeries ohlcSeries = new OHLCSeries("Price");
		// add data
		//sidList.stream().forEach(sid -> ohlcSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getOpen(),
			//	sid.getStockPrice().getHigh(), sid.getStockPrice().getLow(), sid.getStockPrice().getClose()));
		priceDataset.addSeries(ohlcSeries);
		return priceDataset;
	}

	@Override
	protected TimeSeriesCollection createSMADataset() {
		TimeSeriesCollection smaDataset = new TimeSeriesCollection();
		TimeSeries sma60Series = new TimeSeries("SMA_60");
		TimeSeries sma20Series = new TimeSeries("SMA_20");
		TimeSeries sma10Series = new TimeSeries("SMA_10");
		TimeSeries sma5Series = new TimeSeries("SMA_5");
		// add data
		/*sidList.stream().forEach(sid -> {
			Date date = sid.getTradingDate();
			sma60Series.add(new Day(date), sid.getSma60());
			sma20Series.add(new Day(date), sid.getSma20());
			sma10Series.add(new Day(date), sid.getSma10());
			sma5Series.add(new Day(date), sid.getSma5());
		});*/
		// add data
		smaDataset.addSeries(sma5Series);
		smaDataset.addSeries(sma10Series);
		smaDataset.addSeries(sma20Series);
		smaDataset.addSeries(sma60Series);
		return smaDataset;
	}

}

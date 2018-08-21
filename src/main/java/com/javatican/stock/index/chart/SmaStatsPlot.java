package com.javatican.stock.index.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.IndexStrategyDAO;


@Component("smaStatsPlot")
public class SmaStatsPlot  {

	public SmaStatsPlot() {
	}
	
	@Autowired
	IndexStrategyDAO indexStrategyDAO;
 
	private Map<Date, List<Integer>> statsMap;

	public Plot getPlot() throws StockException {
		statsMap = indexStrategyDAO.loadSmaStatsData();
		return createPlot();
	}
 
	 
	private XYPlot createPlot() {
		TimeSeriesCollection ssDataset = createSmaStatsDataset();
		//  
		NumberAxis ssAxis = new NumberAxis("数量");
		ssAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ssAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//  
		XYLineAndShapeRenderer ssRenderer = new XYLineAndShapeRenderer();
		ssRenderer.setDefaultShapesVisible(false);
		ssRenderer.setSeriesStroke(0, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {2.0f, 6.0f}, 0.0f
            ));
		ssRenderer.setSeriesPaint(0, Color.MAGENTA);
		ssRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
		ssRenderer.setSeriesPaint(1, Color.RED);
		ssRenderer.setSeriesStroke(2, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {2.0f, 6.0f}, 0.0f
            ));
		ssRenderer.setSeriesPaint(2, Color.BLUE);
		ssRenderer.setSeriesStroke(3, new BasicStroke(2.0f));
		ssRenderer.setSeriesPaint(3, Color.GREEN);
		XYPlot ssSubplot = new XYPlot(ssDataset, null, ssAxis, ssRenderer);
		ssSubplot.setBackgroundPaint(Color.WHITE); 
		ssSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		return ssSubplot;
	}
 
	private TimeSeriesCollection createSmaStatsDataset() {
		TimeSeriesCollection ssDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("Above20");
		TimeSeries series2 = new TimeSeries("Above60");
		TimeSeries series3 = new TimeSeries("20Up");
		TimeSeries series4 = new TimeSeries("60Up");
		// add data
		statsMap.entrySet().stream().forEach(entry->{
			series1.add(new Day(entry.getKey()), entry.getValue().get(0));
			series2.add(new Day(entry.getKey()), entry.getValue().get(1));
			series3.add(new Day(entry.getKey()), entry.getValue().get(2));
			series4.add(new Day(entry.getKey()), entry.getValue().get(3));
		});
		// add data
		ssDataset.addSeries(series1);
		ssDataset.addSeries(series2);
		ssDataset.addSeries(series3);
		ssDataset.addSeries(series4);
		return ssDataset;
	}

}

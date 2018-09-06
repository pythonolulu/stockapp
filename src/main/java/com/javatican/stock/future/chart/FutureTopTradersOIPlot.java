package com.javatican.stock.future.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

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
import com.javatican.stock.dao.FutureDataDAO;
import com.javatican.stock.model.FutureData;

@Component("fttoiPlot")
public class FutureTopTradersOIPlot {

	@Autowired
	FutureDataDAO futureDataDAO;

	private List<FutureData> fdList;

	public Plot getPlot() throws StockException {
		fdList = futureDataDAO.findAll();
		return createPlot();
	}

	public FutureTopTradersOIPlot() {
	}

	private XYPlot createPlot() {
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
		renderer1.setSeriesShape(0, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(1, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(1, Color.GREEN);
		renderer1.setSeriesShape(1, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(2, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(2, Color.BLACK);
		renderer1.setSeriesShape(2, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		//
		renderer1.setSeriesStroke(3, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(3, Color.RED);
		renderer1.setSeriesShape(3, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(4, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(4, Color.ORANGE);
		renderer1.setSeriesShape(4, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		renderer1.setSeriesStroke(5, new BasicStroke(1.0f));
		renderer1.setSeriesPaint(5, Color.BLUE);
		renderer1.setSeriesShape(5, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
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
		fdList.stream().forEach(fd -> {
			series1.add(new Day(fd.getTradingDate()), fd.getBuyOiTop10All());
			series2.add(new Day(fd.getTradingDate()), fd.getBuyOiTop5All());
			series3.add(new Day(fd.getTradingDate()), fd.getBuyOiTop10All() - fd.getBuyOiTop5All());
			series4.add(new Day(fd.getTradingDate()), -1 * fd.getSellOiTop10All());
			series5.add(new Day(fd.getTradingDate()), -1 * fd.getSellOiTop5All());
			series6.add(new Day(fd.getTradingDate()), -1 * (fd.getSellOiTop10All() - fd.getSellOiTop5All()));
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

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

@Component("fttcmoiPlot")
public class FutureTopTradersCurrentMonthOIPlot {

	@Autowired
	FutureDataDAO futureDataDAO;

	private List<FutureData> fdList;

	public Plot getPlot() throws StockException {
		fdList = futureDataDAO.findAll();
		return createPlot();
	}

	public FutureTopTradersCurrentMonthOIPlot() {
	}

	private XYPlot createPlot() {
		TimeSeriesCollection foDataset = createOIDataset();
		// foreign OI axis
		NumberAxis foAxis = new NumberAxis("未平仓(口)");
		foAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		foAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		// OI renderer
		XYLineAndShapeRenderer foRenderer = new XYLineAndShapeRenderer();
		foRenderer.setDefaultShapesVisible(true);
		foRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(0, Color.MAGENTA);
		foRenderer.setSeriesShape(0, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(1, Color.GREEN);
		foRenderer.setSeriesShape(1, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(2, Color.BLACK);
		foRenderer.setSeriesShape(2, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		//
		foRenderer.setSeriesStroke(3, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(3, Color.RED);
		foRenderer.setSeriesShape(3, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(4, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(4, Color.ORANGE);
		foRenderer.setSeriesShape(4, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		foRenderer.setSeriesStroke(5, new BasicStroke(1.0f));
		foRenderer.setSeriesPaint(5, Color.BLUE);
		foRenderer.setSeriesShape(5, new Ellipse2D.Double(-1d, -1d, 2d, 2d));
		// Create foreign Subplot
		XYPlot foSubplot = new XYPlot(foDataset, null, foAxis, foRenderer);
		foSubplot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
		foSubplot.setBackgroundPaint(Color.WHITE);
		return foSubplot;
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
			series1.add(new Day(fd.getTradingDate()), fd.getBuyOiTop10());
			series2.add(new Day(fd.getTradingDate()), fd.getBuyOiTop5());
			series3.add(new Day(fd.getTradingDate()), fd.getBuyOiTop10() - fd.getBuyOiTop5());
			series4.add(new Day(fd.getTradingDate()), -1 * fd.getSellOiTop10());
			series5.add(new Day(fd.getTradingDate()), -1 * fd.getSellOiTop5());
			series6.add(new Day(fd.getTradingDate()), -1 * (fd.getSellOiTop10() - fd.getSellOiTop5()));
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

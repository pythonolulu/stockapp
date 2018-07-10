package com.javatican.stock.chart;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

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
import com.javatican.stock.dao.DealerTradeSummaryDAO;
import com.javatican.stock.model.DealerTradeSummary;
import com.javatican.stock.model.StockItem;

@Component("iptvPlot")
public class InvestorPutTradeValuePlot implements JPlot {

	@Autowired
	DealerTradeSummaryDAO dealerTradeSummaryDAO;
	private List<DealerTradeSummary> dtsList;

	public InvestorPutTradeValuePlot() {
	}
	
	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.dtsList = dealerTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
		if(dtsList.isEmpty()) return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection iptvDataset = this.createInvestorPutTradeValueDataset();
		TimeSeriesCollection ipnvDataset = this.createInvestorPutNetValueDataset();
		//
		NumberAxis iptvAxis = new NumberAxis("买卖金额-認售(百万)");
		iptvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		iptvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer iptvRenderer = new XYBarRenderer();
		iptvRenderer.setShadowVisible(false);
		iptvRenderer.setSeriesPaint(0, Color.CYAN);
		iptvRenderer.setSeriesPaint(1, Color.PINK);
		//
		XYPlot iptvSubplot = new XYPlot(iptvDataset, null, iptvAxis, iptvRenderer);
		iptvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		iptvSubplot.setDataset(1, ipnvDataset);
		//
		XYLineAndShapeRenderer ipnvRenderer = new XYLineAndShapeRenderer();
		ipnvRenderer.setSeriesPaint(0, Color.RED);
		ipnvRenderer.setSeriesLinesVisible(0, false);
		ipnvRenderer.setSeriesShapesVisible(0, true);
		ipnvRenderer.setSeriesShape(0, new Ellipse2D.Double(-2d, -2d, 4d, 4d));
		//
		ipnvRenderer.setSeriesPaint(1, Color.BLACK);
		ipnvRenderer.setSeriesLinesVisible(1, false);
		ipnvRenderer.setSeriesShapesVisible(1, true);
		Shape tri = ShapeUtils.createDownTriangle(1.5F);
		ipnvRenderer.setSeriesShape(1, tri);
		iptvSubplot.setRenderer(1, ipnvRenderer);
		iptvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return iptvSubplot;
	}


	private TimeSeriesCollection createInvestorPutTradeValueDataset() {
		TimeSeriesCollection iptvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("散户买(認售)");
		TimeSeries series2 = new TimeSeries("散户卖(認售)");
		// add data
		dtsList.stream().forEach(dts -> {
			series1.add(new Day(dts.getTradingDate()), dts.getHedgePutSell() / 1000000);
			series2.add(new Day(dts.getTradingDate()), -1 * dts.getHedgePutBuy() / 1000000);
		});
		// add data
		iptvDataset.addSeries(series1);
		iptvDataset.addSeries(series2);
		return iptvDataset;
	}

	private TimeSeriesCollection createInvestorPutNetValueDataset() {
		TimeSeriesCollection ipnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("散户净买(認售)");
		TimeSeries series2 = new TimeSeries("散户净卖(認售)");
		// add data
		dtsList.stream().forEach(dts -> {
			double amount = dts.getHedgePutNet();
			if (amount < 0) {
				series1.add(new Day(dts.getTradingDate()), -1 * amount / 1000000);
			} else {
				series2.add(new Day(dts.getTradingDate()), -1 * amount / 1000000);
			}
		});
		// add data
		ipnvDataset.addSeries(series1);
		ipnvDataset.addSeries(series2);
		return ipnvDataset;
	}
}

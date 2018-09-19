package com.javatican.stock.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
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
import com.javatican.stock.dao.DealerTradeSummaryDAO; 
import com.javatican.stock.model.DealerTradeSummary;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.util.StockChartUtils;

@Component("ictvPlot")
public class InvestorCallTradeValuePlot implements JPlot {

	@Autowired
	DealerTradeSummaryDAO dealerTradeSummaryDAO;
	private List<DealerTradeSummary> dtsList;

	public InvestorCallTradeValuePlot() {
	}
	
	@Override
	public Plot getPlot(StockItem stockItem) throws StockException {
		this.dtsList = dealerTradeSummaryDAO.getByStockSymbol(stockItem.getSymbol());
		if(dtsList.isEmpty()) return null;
		return createPlot();
	}

	private XYPlot createPlot() {
		TimeSeriesCollection ictvDataset = this.createInvestorCallTradeValueDataset();
		TimeSeriesCollection icnvDataset = this.createInvestorCallNetValueDataset();
		TimeSeriesCollection dsnbDataset = this.createDealerStockNetBuyDataset();
		//
		NumberAxis ictvAxis = new NumberAxis("买卖金额-認購(百万)");
		ictvAxis.setAutoRangeIncludesZero(true);
		// Set to no decimal
		ictvAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
		//
		XYBarRenderer ictvRenderer = new XYBarRenderer();
		ictvRenderer.setShadowVisible(false);
		ictvRenderer.setSeriesPaint(0, Color.CYAN);
		ictvRenderer.setSeriesPaint(1, Color.PINK);
		//
		XYPlot ictvSubplot = new XYPlot(ictvDataset, null, ictvAxis, ictvRenderer);
		ictvSubplot.setBackgroundPaint(Color.WHITE);
		// 2nd dataset
		ictvSubplot.setDataset(1, icnvDataset);
		//
		XYLineAndShapeRenderer icnvRenderer = new XYLineAndShapeRenderer();
		icnvRenderer.setSeriesPaint(0, Color.RED);
		icnvRenderer.setSeriesLinesVisible(0, false);
		icnvRenderer.setSeriesShapesVisible(0, true);
		icnvRenderer.setSeriesShape(0, StockChartUtils.getSolidSphereShapeLarge());
		//
		icnvRenderer.setSeriesPaint(1, Color.BLACK);
		icnvRenderer.setSeriesLinesVisible(1, false);
		icnvRenderer.setSeriesShapesVisible(1, true);
		icnvRenderer.setSeriesShape(1, StockChartUtils.getSolidSphereShapeLarge());
		ictvSubplot.setRenderer(1, icnvRenderer);
		// 3rd dataset
		NumberAxis dsnbAxis = new NumberAxis("自营商标的累积净买(百万)");
		dsnbAxis.setAutoRangeIncludesZero(true);
		ictvSubplot.setRangeAxis(1, dsnbAxis);
		ictvSubplot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		// 3rd dataset
		ictvSubplot.setDataset(2, dsnbDataset);
		// map the 3rd dataset to 3rd axis
		ictvSubplot.mapDatasetToRangeAxis(2, 1);
		//
		XYLineAndShapeRenderer dsnbRenderer = new XYLineAndShapeRenderer();
		dsnbRenderer.setDefaultShapesVisible(false);
		dsnbRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
		dsnbRenderer.setSeriesPaint(0, Color.GRAY);
		ictvSubplot.setRenderer(2, dsnbRenderer);
		ictvSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return ictvSubplot;
	}


	private TimeSeriesCollection createInvestorCallTradeValueDataset() {
		TimeSeriesCollection ictvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("散户买(認購)");
		TimeSeries series2 = new TimeSeries("散户卖(認購)");
		// add data
		dtsList.stream().forEach(dts -> {
			series1.add(new Day(dts.getTradingDate()), dts.getHedgeCallSell() / 1000000);
			series2.add(new Day(dts.getTradingDate()), -1 * dts.getHedgeCallBuy() / 1000000);
		});
		// add data
		ictvDataset.addSeries(series1);
		ictvDataset.addSeries(series2);
		return ictvDataset;
	}

	private TimeSeriesCollection createInvestorCallNetValueDataset() {
		TimeSeriesCollection icnvDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("散户净买(認購)");
		TimeSeries series2 = new TimeSeries("散户净卖(認購)");
		// add data
		dtsList.stream().forEach(dts -> {
			double amount = dts.getHedgeCallNet();
			if (amount < 0) {
				series1.add(new Day(dts.getTradingDate()), -1 * amount / 1000000);
			} else {
				series2.add(new Day(dts.getTradingDate()), -1 * amount / 1000000);
			}
		});
		// add data
		icnvDataset.addSeries(series1);
		icnvDataset.addSeries(series2);
		return icnvDataset;
	}

	private TimeSeriesCollection createDealerStockNetBuyDataset() {
		TimeSeriesCollection dsnbDataset = new TimeSeriesCollection();
		TimeSeries series1 = new TimeSeries("自营商标的累积净买");
		// add data
		double prevHedgeNetSum = 0.0;
		for (DealerTradeSummary dts : dtsList) {
			double newHedgeNetSum = prevHedgeNetSum + dts.getHedgeNet() / 1000000;
			series1.add(new Day(dts.getTradingDate()), newHedgeNetSum);
			prevHedgeNetSum = newHedgeNetSum;
		}
		// add data
		dsnbDataset.addSeries(series1);
		return dsnbDataset;
	}
}

package com.javatican.stock.chart;

import org.jfree.chart.plot.Plot;

import com.javatican.stock.StockException;
import com.javatican.stock.model.StockItem;

public interface JPlot {
	public Plot getPlot(StockItem stockItem) throws StockException;
}
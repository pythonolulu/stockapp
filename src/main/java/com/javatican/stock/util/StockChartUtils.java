package com.javatican.stock.util;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

public class StockChartUtils {

	public static void showAnnotation(XYPlot p, double x, String text) {
		Range r = p.getRangeAxis(0).getRange();
		double y = (r.getLowerBound() + r.getUpperBound()) / 2;
		final XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
		annotation.setTextAnchor(TextAnchor.CENTER_LEFT);
		annotation.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 24));
		p.addAnnotation(annotation);
	}
}

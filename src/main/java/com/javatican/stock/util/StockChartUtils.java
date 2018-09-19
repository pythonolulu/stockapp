package com.javatican.stock.util;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Date;
import java.util.List;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.ShapeUtils;
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

	public static void drawHorizontalValueMarker(XYPlot p, int index, double position) {
		ValueMarker marker = new ValueMarker(position); // position is the value on the axis
		marker.setPaint(Color.BLACK);
		// marker.setLabel("here"); // see JavaDoc for labels, colors, strokes
		p.addRangeMarker(index, marker, Layer.FOREGROUND);
		//p.addDomainMarker(marker);
	}

	public static void drawVerticalValueMarkerForDate(XYPlot p, Date d) {
		ValueMarker marker = new ValueMarker((double) d.getTime()); // position is the value on the axis
		marker.setPaint(Color.BLACK);
		// marker.setLabel("here"); // see JavaDoc for labels, colors, strokes
		//p.addRangeMarker(marker);
		p.addDomainMarker(marker);
	}

	public static void drawVerticalValueMarkersForDateList(XYPlot p, List<Date> dList) {
		for (Date d : dList) {
			ValueMarker marker = new ValueMarker((double) d.getTime()); // position is the value on the axis
			marker.setPaint(Color.BLACK);
			// marker.setLabel("here"); // see JavaDoc for labels, colors, strokes
			//p.addRangeMarker(marker);
			p.addDomainMarker(marker);
		}
	}
	
	public static Ellipse2D.Double getSolidSphereShape(){
		return new Ellipse2D.Double(-1d, -1d, 2d, 2d);
	}

	public static Ellipse2D.Double getSolidSphereShapeLarge(){
		return new Ellipse2D.Double(-2d, -2d, 4d, 4d);
	}

	public static Shape getDownTriangleShape(){
		return ShapeUtils.createDownTriangle(1.5F);
	} 
	
	public static Shape getUpTriangleShape(){
		return ShapeUtils.createUpTriangle(1.5F);
	} 
}

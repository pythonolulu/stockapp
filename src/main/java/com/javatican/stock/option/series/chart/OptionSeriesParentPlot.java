package com.javatican.stock.option.series.chart;

import java.util.Date;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.OptionSeriesDataDAO;
import com.javatican.stock.model.OptionSeriesData;

@Component("ospPlot")
public abstract class OptionSeriesParentPlot {

	@Autowired
	OptionSeriesDataDAO optionSeriesDataDAO;

	protected List<OptionSeriesData> osList;

	protected abstract XYPlot createPlot();

	public Plot getPlot(Date dateSince, boolean callOption, Integer strikePrice, String type) throws StockException {
		if (dateSince != null) {
			if (callOption) {
				switch (type) {
				case "week":
					osList = optionSeriesDataDAO.findWeekCallOptionByStrikePriceDateSince(strikePrice, dateSince);
					break;
				case "currentMonth":
					osList = optionSeriesDataDAO.findCurrentMonthCallOptionByStrikePriceDateSince(strikePrice,
							dateSince);
					break;
				case "nextMonth":
					osList = optionSeriesDataDAO.findNextMonthCallOptionByStrikePriceDateSince(strikePrice, dateSince);
					break;
				}
			} else {
				switch (type) {
				case "week":
					osList = optionSeriesDataDAO.findWeekPutOptionByStrikePriceDateSince(strikePrice, dateSince);
					break;
				case "currentMonth":
					osList = optionSeriesDataDAO.findCurrentMonthPutOptionByStrikePriceDateSince(strikePrice,
							dateSince);
					break;
				case "nextMonth":
					osList = optionSeriesDataDAO.findNextMonthPutOptionByStrikePriceDateSince(strikePrice, dateSince);
					break;
				}
			}
		} else {
			if (callOption) {
				switch (type) {
				case "week":
					osList = optionSeriesDataDAO.findWeekCallOptionByStrikePrice(strikePrice);
					break;
				case "currentMonth":
					osList = optionSeriesDataDAO.findCurrentMonthCallOptionByStrikePrice(strikePrice);
					break;
				case "nextMonth":
					osList = optionSeriesDataDAO.findNextMonthCallOptionByStrikePrice(strikePrice);
					break;
				}
			} else {
				switch (type) {
				case "week":
					osList = optionSeriesDataDAO.findWeekPutOptionByStrikePrice(strikePrice);
					break;
				case "currentMonth":
					osList = optionSeriesDataDAO.findCurrentMonthPutOptionByStrikePrice(strikePrice);
					break;
				case "nextMonth":
					osList = optionSeriesDataDAO.findNextMonthPutOptionByStrikePrice(strikePrice);
					break;
				}
			}
		}
		return createPlot();
	}
}

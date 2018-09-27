package com.javatican.stock.option.chart;

import java.util.Date;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.OptionDataDAO;
import com.javatican.stock.model.OptionData;

@Component("opPlot")
public abstract class OptionParentPlot {

	@Autowired
	OptionDataDAO optionDataDAO;

	protected List<OptionData> odList;
	
	protected abstract XYPlot createPlot();
	
	public Plot getPlot() throws StockException {
		odList = optionDataDAO.findAll();
		return createPlot();
	}

	public Plot getPlot(Date dateSince) throws StockException {
		if (dateSince != null) {
			odList = optionDataDAO.findAfter(dateSince);
		} else {
			odList = optionDataDAO.findAll();
		}
		return createPlot();
	}
}

package com.javatican.stock;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.service.OptionChartService;
import com.javatican.stock.service.OptionSeriesChartService;
import com.javatican.stock.service.OptionService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class OptionController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
	@Autowired
	private OptionService optionService;
	@Autowired
	private OptionChartService optionChartService;
	@Autowired
	private OptionSeriesChartService optionSeriesChartService;

	@GetMapping("/updateOptionData")
	public ResponseMessage updateOptionData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			optionService.updateData();
			mes.setCategory("Success");
			mes.setText("Option data have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Option data fail to be updated.");
		}
		return mes;
	}

	@GetMapping("/getOptionChart")
	public ModelAndView getOptionChart(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "dateSince", required = false) String dateSinceString) {
		try {
			Date dateSince = null;
			if (dateSinceString != null) {
				dateSince = StockUtils.stringSimpleToDate(dateSinceString).get();
			}
			String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
			optionChartService.createGraph(force, latestTradingDateString, dateSince);
			return new ModelAndView("redirect:" + "/stock/imgs/option/option_" + latestTradingDateString + ".png");
		} catch (Exception e) {
			return new ModelAndView("stock/error");
		}
	}

	@GetMapping("/getOptionStrategyChart")
	public ModelAndView getOptionStrategyChart(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "dateSince", required = false) String dateSinceString) {
		try {
			Date dateSince = null;
			if (dateSinceString != null) {
				dateSince = StockUtils.stringSimpleToDate(dateSinceString).get();
			}
			String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
			optionChartService.createGraph(force, latestTradingDateString, dateSince);
			return new ModelAndView(
					"redirect:" + "/stock/imgs/option/option_strategy_" + latestTradingDateString + ".png");
		} catch (Exception e) {
			return new ModelAndView("stock/error");
		}
	}

	@GetMapping("/getOptionStrategy2Chart")
	public ModelAndView getOptionStrategy2Chart(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "dateSince", required = false) String dateSinceString) {
		try {
			Date dateSince = null;
			if (dateSinceString != null) {
				dateSince = StockUtils.stringSimpleToDate(dateSinceString).get();
			}
			String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
			optionChartService.createGraph(force, latestTradingDateString, dateSince);
			return new ModelAndView(
					"redirect:" + "/stock/imgs/option/option_strategy2_" + latestTradingDateString + ".png");
		} catch (Exception e) {
			return new ModelAndView("stock/error");
		}
	}

	@GetMapping("/getOptionSeriesChart")
	public ModelAndView getOptionSeriesChart(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "dateSince", required = false) String dateSinceString,
			@RequestParam(value = "upRange", defaultValue = "1000") int upRange,
			@RequestParam(value = "downRange", defaultValue = "500") int downRange) {
		try {
			Date dateSince = null;
			if (dateSinceString != null) {
				dateSince = StockUtils.stringSimpleToDate(dateSinceString).get();
			}
			String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
			optionSeriesChartService.createGraph(force, latestTradingDateString, dateSince, upRange, downRange);
			return new ModelAndView("redirect:" + "/stock/imgs/option/series/option_" + latestTradingDateString + ".png");
		} catch (Exception e) {
			return new ModelAndView("stock/error");
		}
	}
}

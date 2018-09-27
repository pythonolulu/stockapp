package com.javatican.stock;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.service.FutureChartService;
import com.javatican.stock.service.FutureService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class FutureController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()); 
	@Autowired
	private StockService stockService;
	@Autowired
	private FutureService futureService;

	@Autowired
	private FutureChartService futureChartService;

	@GetMapping("/updateFutureData")
	public ResponseMessage updateFutureData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			futureService.updateData();
			mes.setCategory("Success");
			mes.setText("Future data have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Future data fail to be updated.");
		}
		return mes;
	}

	@GetMapping("/getFutureChart")
	public ModelAndView getFutureChart(@RequestParam(value = "force", defaultValue = "false") boolean force) {
		String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
		futureChartService.createGraph(force, latestTradingDateString);
		return new ModelAndView("redirect:" + "/stock/imgs/future/future_" + latestTradingDateString + ".png");
	}

}

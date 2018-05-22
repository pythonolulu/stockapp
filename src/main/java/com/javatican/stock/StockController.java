package com.javatican.stock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javatican.stock.util.ResponseMessage;

@RestController
@RequestMapping("stock/*")
public class StockController {
 

	@Autowired
	private StockService stockService;

	@GetMapping
	public ResponseMessage updateTradingDate() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockService.storeTradingDate();
			mes.setCategory("Success");
			mes.setText("Trading date information has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading date information fails to be updated.");
		}
		return mes;
		
	}

}
  